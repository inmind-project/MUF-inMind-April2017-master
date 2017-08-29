package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.collect.ImmutableList;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserFrame;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.sara.repo.UserModelRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserModelComponentTest {
    private static final String SESSION_ID = "session_id";

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private final UserFrame userFrame = Utils.fromJson("{\"frame\":{\"actors\":{\"like\":[{\"entity\":\"actors\",\"polarity\":0.0,\"value\":\"jennifer lawrence\",\"id\":\"jennifer_iii_lawrence\",\"start\":0,\"end\":17}],\"dislike\":[]}," +
            "\"genres\":{\"like\":[],\"dislike\":[]},\"directors\":{\"like\":[],\"dislike\":[]}," +
            "\"movies\":{\"like\":[\"The Hateful Eight (2015)\",\"Django Unchained (2012)\"],\"dislike\":[]," +
            "\"history\":[\"The Hateful Eight (2015)\"]}},\"ask_stack\":[\"genres\",\"directors\",\"actors\",\"recommend\"]," +
            "\"universals\":[\"help\",\"start_over\"],\"latestUtterance\":\"Hello\"}", UserFrame.class);
    private final ImmutableList<String> behaviorNetworkStates = ImmutableList.of("state_1", "state_2");

    private UserModelComponent component;
    private UserModel model;
    @Mock private UserModelRepository repo;
    @Mock private Blackboard blackboard;

    @Before
    public void before() {
        model = new UserModel(SESSION_ID);
        model.setUserFrame(userFrame);
        model.updateBehaviorNetworkStates(behaviorNetworkStates);

        component = spy(new UserModelComponent());

        doReturn(repo).when(component).createRepo();
        doReturn(SESSION_ID).when(component).getSessionId();
        doReturn(blackboard).when(component).blackboard();

        when(repo.readModel()).thenReturn(Optional.of(model));
    }

    @Test
    public void readsModelWhenSessionStarted() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, null));
        verify(repo).readModel();

        verify(blackboard).post(component, SaraCons.MSG_USER_MODEL_LOADED, model);
    }

    @Test
    public void handlesClearEpisodic() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, UserModelComponent.ResetOptions.EPISODIC));
        assertEquals(userFrame, model.getUserFrame());
        assertFalse(userFrame.getFrame().getActors().getLike().isEmpty());
        assertEquals(ImmutableList.of(), model.getBehaviorNetworkStates());

        verify(blackboard).post(component, SaraCons.MSG_USER_MODEL_LOADED, model);
    }

    @Test
    public void handlesClearSemantic() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, UserModelComponent.ResetOptions.SEMANTIC));
        assertTrue(userFrame.getFrame().getActors().getLike().isEmpty());
        assertEquals(behaviorNetworkStates, model.getBehaviorNetworkStates());

        verify(blackboard).post(component, SaraCons.MSG_USER_MODEL_LOADED, model);
    }

    @Test
    public void handlesClearAll() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, UserModelComponent.ResetOptions.ALL));

        final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(blackboard).post(eq(component), eq(SaraCons.MSG_USER_MODEL_LOADED), captor.capture());
        final UserModel captured = captor.getValue();
        assertTrue(userFrame.getFrame().getActors().getLike().isEmpty());
        assertEquals(ImmutableList.of(), captured.getBehaviorNetworkStates());
    }

    @Test
    public void writesModelOnShutDown() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, null));
        component.shutDown();
        verify(repo).writeModel(model);
    }

    @Test
    public void updatesModelOnSREvent() throws Exception {
        component.onEvent(new BlackboardEvent("status", SaraCons.MSG_START_SESSION, null));

        final SROutput srOutput = new SROutput();
        final ImmutableList<String> states = ImmutableList.of("new_state_1", "new_state_2");
        srOutput.setStates(states);
        final BlackboardEvent event = new BlackboardEvent("status", SaraCons.MSG_SR, srOutput);

        component.onEvent(event);

        assertEquals(states, model.getBehaviorNetworkStates());
    }

}