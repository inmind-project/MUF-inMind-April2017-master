package edu.cmu.inmind.multiuser.sara.repo;

import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UserModelRepository {
    private static final String FILE_NAME_PATTERN = "user_model_%s.json";
    private static final String PATH_PATTERN = "%s/user_models/";

    @NotNull private final String path;
    @NotNull private final String fileName;
    @NotNull private final String sessionId;

    public UserModelRepository(@NotNull String logPath, @NotNull String sessionId) {
        this.path = String.format(PATH_PATTERN, logPath);
        // The session id remains stable across sessions with a given client so we use this to look up the user model
        this.fileName = String.format(FILE_NAME_PATTERN, sessionId);
        this.sessionId = sessionId;
    }

    @NotNull
    public Optional<UserModel> readModel() {
        try {
            return Optional.ofNullable(Utils.fromJsonFile(getFilePath(), UserModel.class))
                    .filter(model -> model.getId().equals(sessionId));
        } catch (@NotNull Exception exception) {
            Log4J.error(this, "Unable to load user userModel for " + sessionId);
            exception.printStackTrace();
        }
        return Optional.empty();
    }

    public void writeModel(@NotNull UserModel model) {
        Utils.toJsonFile(model, path, fileName);
    }


    @NotNull
    private String getFilePath() {
        return path + fileName;
    }
}
