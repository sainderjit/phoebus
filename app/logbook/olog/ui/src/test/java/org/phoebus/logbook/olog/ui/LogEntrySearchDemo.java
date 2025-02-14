package org.phoebus.logbook.olog.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.phoebus.framework.nls.NLS;
import org.phoebus.logbook.Attachment;
import org.phoebus.logbook.AttachmentImpl;
import org.phoebus.logbook.LogClient;
import org.phoebus.logbook.LogEntry;
import org.phoebus.logbook.LogEntryImpl.LogEntryBuilder;
import org.phoebus.logbook.Logbook;
import org.phoebus.logbook.LogbookException;
import org.phoebus.logbook.LogbookImpl;
import org.phoebus.logbook.SearchResult;
import org.phoebus.logbook.Tag;
import org.phoebus.logbook.TagImpl;
import org.phoebus.logbook.olog.ui.query.OlogQuery;
import org.phoebus.logbook.olog.ui.query.OlogQueryManager;
import org.phoebus.logbook.olog.ui.write.LogEntryEditorStage;
import org.phoebus.ui.javafx.ApplicationWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogEntrySearchDemo extends ApplicationWrapper {

    public static void main(String[] args) {
        launch(LogEntrySearchDemo.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        File tmp = File.createTempFile("demo", "suffix");
        tmp.deleteOnExit();
        OlogQueryManager ologQueryManager = OlogQueryManager.getInstance(tmp);
        ologQueryManager.getOrAddQuery(new OlogQuery("start=1 week&end=now"));
        SearchParameters searchParameters = new SearchParameters();
        ResourceBundle resourceBundle = NLS.getMessages(Messages.class);
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(resourceBundle);
        loader.setLocation(this.getClass().getResource("LogEntryTableView.fxml"));
        loader.setControllerFactory(clazz -> {
            try {
                if (clazz.isAssignableFrom(LogEntryTableViewController.class)) {
                    return clazz.getConstructor(LogClient.class, OlogQueryManager.class, SearchParameters.class)
                            .newInstance(getLogClient(), ologQueryManager, searchParameters);
                } else if (clazz.isAssignableFrom(AdvancedSearchViewController.class)) {
                    return clazz.getConstructor(LogClient.class, SearchParameters.class)
                            .newInstance(getLogClient(), searchParameters);
                } else if (clazz.isAssignableFrom(LogPropertiesController.class)) {
                    return clazz.getConstructor().newInstance();
                } else if (clazz.isAssignableFrom(AttachmentsPreviewController.class)) {
                    return clazz.getConstructor().newInstance();
                } else if (clazz.isAssignableFrom(LogEntryCellController.class)) {
                    return clazz.getConstructor().newInstance();
                } else if (clazz.isAssignableFrom(LogEntryDisplayController.class)) {
                    return clazz.getConstructor().newInstance();
                } else if (clazz.isAssignableFrom(MergedLogEntryDisplayController.class)) {
                    return clazz.getConstructor(LogClient.class).newInstance(getLogClient());
                } else if (clazz.isAssignableFrom(SingleLogEntryDisplayController.class)) {
                    return clazz.getConstructor(LogClient.class).newInstance(getLogClient());
                } else {
                    throw new RuntimeException("No controller for class " + clazz.getName());
                }
            } catch (Exception e) {
                Logger.getLogger(LogEntryEditorStage.class.getName()).log(Level.SEVERE, "Failed to construct controller for log calendar view", e);
            }
            return null;
        });
        loader.load();
        Parent root = loader.getRoot();

        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();

        List<LogEntry> logs = new ArrayList<>();

        Set<Tag> tags = new HashSet<>();
        tags.add(TagImpl.of("tag1", "active"));
        tags.add(TagImpl.of("tag2", "active"));

        Set<Logbook> logbooks = new HashSet<>();
        logbooks.add(LogbookImpl.of("logbook1", "active"));
        logbooks.add(LogbookImpl.of("logbook2", "active"));

        File imageFile = new File(this.getClass().getClassLoader().getResource("image_1.png").toURI());
        File textFile = new File(this.getClass().getClassLoader().getResource("file_phoebus.txt").toURI());

        List<File> listOfFiles = Arrays.asList(imageFile, textFile);

        for (int i = 0; i < 10; i++) {
            LogEntryBuilder lb = LogEntryBuilder.log()
                    .owner("Owner")
                    .title("log " + i)
                    .description("First line for log " + i)
                    .createdDate(Instant.now())
                    .inLogbooks(logbooks)
                    .id(Long.valueOf(i))
                    .withTags(tags);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                sb.append("Some additional log text");
            }
            lb.appendDescription(sb.toString());
            listOfFiles.forEach(file -> {
                try {
                    lb.attach(AttachmentImpl.of(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
            logs.add(lb.build());
        }
    }

    private LogClient getLogClient() {
        return new LogClient() {
            @Override
            public LogEntry set(LogEntry log) throws LogbookException {
                return null;
            }

            @Override
            public LogEntry getLog(Long logId) {
                return null;
            }

            @Override
            public Collection<Attachment> listAttachments(Long logId) {
                return null;
            }

            @Override
            public List<LogEntry> findLogs(Map<String, String> map) {
                return null;
            }

            @Override
            public Collection<LogEntry> listLogs() {
                return null;
            }

            @Override
            public SearchResult search(Map<String, String> map){
                LogEntry logEntry = LogEntryBuilder.log()
                        .createdDate(Instant.now())
                        .description("desc").build();
                return SearchResult.of(Arrays.asList(logEntry), 1);
            }
        };
    }
}
