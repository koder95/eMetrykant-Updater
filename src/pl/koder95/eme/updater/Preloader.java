/*
 * Copyright (C) 2018 Kamil Jan Mularski [@koder95]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.koder95.eme.updater;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import pl.koder95.eme.updater.services.PreloaderService;

import java.io.IOException;

/**
 * Klasa rozszerza {@link javafx.application.Preloader}. Umożliwia również
 * przygotowanie aplikacji, aby została uruchomiona we właściwej konfiguracji.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public class Preloader extends javafx.application.Preloader {

    @Override
    public void init() {
        System.out.println("Preloader#init");
        GHRepository updaterRepo = GitRepository.E_METRYKANT_UPDATER.connect(GitHubConnection.get());
        if (updaterRepo == null) {
            System.err.println("Updater repository is null.");
            return;
        } else {
            System.out.println("Updater repository connected.");
        }
        GHRelease latestRelease;
        try {
            latestRelease = updaterRepo.getLatestRelease();
        } catch (IOException ex) {
            System.err.println("The latest release cannot be checked.");
            System.err.println("Checking latest version aborted.");
            return;
        }
        if (latestRelease == null) {
            System.err.println("Latest release is null.");
            return;
        }
        String tagName = latestRelease.getTagName();
        if (tagName == null || tagName.isEmpty()) return;
        Version latest = Version.parse(latestRelease.getTagName());
        Version current = Version.get();
        if (latest.compareTo(current) > 0) throw new RuntimeException("This updater is old. Download new version" +
                " from " + latestRelease.getAssetsUrl());
    }

    private Stage createProcessStage(Service<?> service) {
        return createProcessStage(service.titleProperty(), service.progressProperty(), service.messageProperty());
    }

    private Stage createProcessStage(ObservableValue<? extends String> titleProperty,
                                     ObservableValue<? extends Number> progressProperty,
                                     ObservableValue<? extends String> messageProperty) {
        Label title = new Label();
        title.textProperty().bind(titleProperty);
        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(progressProperty);
        Label message = new Label();
        message.textProperty().bind(messageProperty);
        return createProcessStage(title, bar, message);
    }

    private Stage createProcessStage(Label title, ProgressIndicator indicator, Label message) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        VBox root = new VBox(title, indicator, message);
        indicator.setMinWidth(400);
        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root));
        return stage;
    }

    private PreloaderService buildPreloaderService(GHRepository repo) {
        System.out.println(repo);
        if (repo == null) return null;

        GHAsset zip;
        String zipNameContains = "eMetrykant";
        try {
            GHRelease latest = repo.getLatestRelease();
            zip = latest.getAssets().stream().reduce(null, (r, c) -> {
                System.out.println(c);
                return c.getName().contains(zipNameContains) && c.getName().endsWith(".zip")? c : r;
            });
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Cannot download eMetrykant.");
            a.show();
            return null;
        }
        System.out.println("Selected: " + zip);
        final String zipURL = zip.getBrowserDownloadUrl();
        final long zipSize = zip.getSize();
        System.out.println("Size of " + zipURL + " in bytes: " + zipSize);

        PreloaderService service = new PreloaderService(zipURL, Main.USER_DIR, zipNameContains + ".zip", zipSize);
        service.init();
        return service;
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        System.out.println("Application state: " + info.getType());
        if (info.getType() == StateChangeNotification.Type.BEFORE_LOAD) {
            if (!Main.EMETRYKANT_JAR.exists()) {
                GHRepository repo = GitRepository.E_METRYKANT.connect(GitHubConnection.get());

                PreloaderService service = buildPreloaderService(repo);
                if (service != null) {
                    Stage stage = createProcessStage(service);
                    service.setOnSucceeded(event -> {
                        if (service.hasSomeTask()) service.restart();
                        else stage.hide();
                    });
                    stage.setOnShowing(event -> service.start());
                    stage.showAndWait();
                }
            } else {
                System.out.println(Main.EMETRYKANT_JAR);
            }

            if (Main.EMETRYKANT_CONV_JAR.exists()) {
                System.out.println(Main.EMETRYKANT_CONV_JAR);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Preloader started.");
    }
}
