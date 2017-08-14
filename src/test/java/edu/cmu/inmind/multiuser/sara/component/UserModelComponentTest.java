package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.collect.ImmutableList;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.sara.repo.UserModelRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserModelComponentTest {
    private static final String SESSION_ID = "session_id";

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private UserModelComponent component;
    private UserModel model;
    @Mock private UserModelRepository repo;
    @Mock private Blackboard blackboard;

    @Before
    public void before() {
        model = new UserModel(SESSION_ID);
        component = spy(new UserModelComponent());

        doReturn(repo).when(component).createRepo();
        doReturn(SESSION_ID).when(component).getSessionId();
        doReturn(blackboard).when(component).blackboard();

        when(repo.readModel()).thenReturn(Optional.of(model));
    }

    @Test
    public void readsModelOnStartUp() {
        component.startUp();
        verify(repo).readModel();

        verify(blackboard).post(component, SaraCons.MSG_USER_MODEL_LOADED, model);
    }

    @Test
    public void writesModelOnShutDown() {
        component.startUp();
        component.shutDown();
        verify(repo).writeModel(model);
    }

    @Test
    public void updatesModelOnSREvent() {
        final SROutput srOutput = new SROutput();
        final ImmutableList<String> states = ImmutableList.of("state_1", "state_2");
        srOutput.setStates(states);
        final BlackboardEvent event = new BlackboardEvent("status", SaraCons.MSG_SR, srOutput);

        component.startUp();
        component.onEvent(event);

        assertEquals(states, model.getBehaviorNetworkStates());
    }
}