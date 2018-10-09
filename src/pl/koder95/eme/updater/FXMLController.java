/*
 * Copyright (C) 2018 Kamil Jan Mularski [@koder95]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either basicVersionLabel 3 of the License, or
 * (at your option) any later basicVersionLabel.
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

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import pl.koder95.eme.updater.services.SequenceService;
import pl.koder95.eme.updater.task.DownloadFile;
import pl.koder95.eme.updater.task.ExtractZip;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.1, 2018-10-09
 * @since 1.0.0
 */
public class FXMLController implements Initializable {

    @FXML
    private ProgressIndicator basicProgress;
    @FXML
    private ProgressIndicator convProgress;
    @FXML
    private Button update;
    @FXML
    private Label basicVersionLabel;
    @FXML
    private Label basicProgressTitle;
    @FXML
    private Label converterProgressTitle;
    @FXML
    private Label basicProgressMessage;
    @FXML
    private Label converterProgressMessage;
    @FXML
    private Label converterVersionLabel;
    @FXML
    private BorderPane converter;

    private Version eme_version, conv_version;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Object emeVersionObj = ((JarAnalyzer) rb.getObject("eMetrykant.jar JarAnalyzer")).getVersion();
        if (emeVersionObj != null) {
            eme_version = Version.parse(emeVersionObj.toString());
            basicVersionLabel.setText("v" + eme_version.toString());
        } else {
            eme_version = null;
            basicVersionLabel.setText("nieznana");
        }
        Object converterVersionObj = ((JarAnalyzer) rb.getObject("Converter.jar JarAnalyzer")).getVersion();
        if (converterVersionObj != null) {
            conv_version = Version.parse(converterVersionObj.toString());
            converterVersionLabel.setText("v" + conv_version.toString());
        } else {
            conv_version = null;
            converterVersionLabel.setText("nieznana");
            converter.setEffect(new GaussianBlur());
            converter.setOpacity(.33);
            converter.setAccessibleHelp("Nie masz dostępu do konwertera. Pobierz go z " +
                    "https://github.com/koder95/eMetrykant-Converter/latest.");
        }
        checkVersions();
    }

    @FXML
    private void checkVersions() {
        GitHub git = GitHubConnection.get();
        checkVersion(GitRepository.E_METRYKANT.connect(git), eme_version, basicVersionLabel, basicProgress);
        checkVersion(GitRepository.E_METRYKANT_CONVERTER.connect(git), conv_version, converterVersionLabel, convProgress);
    }

    private void checkVersion(GHRepository git, Version current, Label label, ProgressIndicator indicator) {
        if (current == null) return;
        indicator.setProgress(Double.NaN);
        GHRelease latest;
        try {
            latest = git != null? git.getLatestRelease() : null;
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Nie można połączyć się z repozytoriami!");
            a.show();
            indicator.setProgress(0);
            return;
        }
        if (latest != null) {
            Version latestVersion = Version.parse(latest.getTagName());
            if (indicator.isDisabled()) indicator.setDisable(false);
            switch (current.compareTo(latestVersion)) {
                case 0:
                    indicator.setProgress(1);
                    break;
                default:
                    indicator.setProgress(0);
                    label.setText('v' + current.toString() + " [v" + latestVersion.toString() + "]");
                    if (update.isDisabled()) update.setDisable(false);
            }
        }
    }

    private Queue<SequenceService> services = new LinkedList<>();

    @FXML
    private void update() {
        GitHub git = GitHubConnection.get();
        update.setDisable(true);
        if (basicProgress.getProgress() < 1) {
            SequenceService service = buildDownloadService(git, GitRepository.E_METRYKANT);
            if (service != null) {
                service.init();
                basicProgress.progressProperty().bind(service.progressProperty());
                basicProgressTitle.textProperty().bind(service.titleProperty());
                basicProgressMessage.textProperty().bind(service.messageProperty());
                service.setOnSucceeded(event -> {
                    if (service.hasSomeTask()) service.restart();
                    else {
                        services.remove(service);
                    }
                });
                services.add(service);
            }
        }
        if (conv_version != null && convProgress.getProgress() < 1) {
            SequenceService service = buildDownloadService(git, GitRepository.E_METRYKANT_CONVERTER);
            if (service != null) {
                service.init();
                convProgress.progressProperty().bind(service.progressProperty());
                converterProgressTitle.textProperty().bind(service.titleProperty());
                converterProgressMessage.textProperty().bind(service.messageProperty());
                service.setOnSucceeded(event -> {
                    if (service.hasSomeTask()) service.restart();
                    else {
                        services.remove(service);
                    }
                });
                services.add(service);
            }
        }
        services.forEach(Service::start);
        restart();
    }

    private void restart() {
        Thread thread = new Thread(() -> {
            while (!services.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Main.restart();
        });
        thread.start();
    }

    private SequenceService buildDownloadService(GHRepository repo, String zipNameContains) {
        System.out.println(repo);
        if (repo == null) return null;

        GHAsset zip;
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
        return new SequenceService() {
            @Override
            public void init() {
                tasks.add(new DownloadFile(zipURL, Main.USER_DIR, zipNameContains + ".zip", zipSize));
                tasks.add(new ExtractZip(new File(Main.USER_DIR, zipNameContains + ".zip")));
            }
        };
    }

    private SequenceService buildDownloadService(GitHub git, GitRepository repo) {
        return buildDownloadService(repo.connect(git), repo.getZipNameContains());
    }
}
