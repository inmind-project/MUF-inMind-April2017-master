package edu.cmu.inmind.multiuser.sara.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.common.collect.ImmutableList;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserFrame;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.sara.repo.UserModelRepository;
import edu.cmu.inmind.multiuser.sara.util.UserModelResetter;

public class UserModelComponentTest {
    private static final String SESSION_ID = "session_id";

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private final UserFrame userFrame = new UserFrame();
    private final ImmutableList<String> behaviorNetworkStates = ImmutableList.of("state_1", "state_2");

    private UserModelComponent component;
    private UserModel model;
    @Mock private UserModelRepository repo;
    @Mock private Blackboard blackboard;

    @Before
    public void before() {
        userFrame.getActors().getLike().add("Samuel Maskell");

        model = new UserModel(SESSION_ID);
        model.setUserFrame(userFrame);
        model.updateBehaviorNetworkStates(behaviorNetworkStates);

        component = spy(new UserModelComponent());

        doReturn(repo).when(component).createRepo();
        doReturn(SESSION_ID).when(component).getSessionId();

        when(repo.readModel()).thenReturn(Optional.of(model));
    }

    @Test
    public void readsModelWhenSessionStarted() throws Throwable {
        sendEvent(SaraCons.MSG_START_SESSION, "");
        verify(repo).readModel();
        verify(blackboard).post(component, SaraCons.MSG_UM, model);

    }

    @Test
    public void handlesClearEpisodic() throws Throwable {
        sendEvent(SaraCons.MSG_START_SESSION, UserModelResetter.ResetOptions.EPISODIC);
        assertEquals(userFrame, model.getUserFrame());
        assertFalse(model.getUserFrame().getActors().getLike().isEmpty());
        assertEquals(ImmutableList.of(), model.getBehaviorNetworkStates());
        verify(blackboard).post(component, SaraCons.MSG_UM, model);
    }

    @Test
    public void handlesClearSemantic() throws Throwable {
        sendEvent(SaraCons.MSG_START_SESSION, UserModelResetter.ResetOptions.SEMANTIC);
        assertTrue(model.getUserFrame().getActors().getLike().isEmpty());
        assertEquals(behaviorNetworkStates, model.getBehaviorNetworkStates());
        verify(blackboard).post(component, SaraCons.MSG_UM, model);
    }

    @Test
    public void handlesClearAll() throws Throwable {
       sendEvent(SaraCons.MSG_START_SESSION, UserModelResetter.ResetOptions.ALL);

        final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(blackboard).post(eq(component), eq(SaraCons.MSG_UM), captor.capture());
        final UserModel captured = captor.getValue();
        assertTrue(model.getUserFrame().getActors().getLike().isEmpty());
        assertEquals(ImmutableList.of(), captured.getBehaviorNetworkStates());
    }

    @Test
    public void writesModelOnShutDown() throws Throwable {
        sendEvent(SaraCons.MSG_START_SESSION, "");
        component.shutDown();
        verify(repo).writeModel(model);
    }

    @Test
    public void updatesModelOnSREvent() throws Throwable {
        sendEvent(SaraCons.MSG_START_SESSION, "");

        final SROutput srOutput = new SROutput(new DMOutput());
        final ImmutableList<String> states = ImmutableList.of("new_state_1", "new_state_2");
        srOutput.setStates(states);

        sendEvent(SaraCons.MSG_SR, srOutput);

        assertEquals(states, model.getBehaviorNetworkStates());
    }

    private void sendEvent(String event, Object element) throws Throwable {
        component.onEvent(blackboard, new BlackboardEvent("status", event, element, SESSION_ID));
    }
}