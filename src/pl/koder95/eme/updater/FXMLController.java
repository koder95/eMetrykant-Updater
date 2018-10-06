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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
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
    }

    @FXML
    private void checkVersions() {
        GitHub git = GitHubConnection.get();
        checkVersion(GitRepository.E_METRYKANT.connect(git), eme_version, basicVersionLabel, basicProgress);
        checkVersion(GitRepository.E_METRYKANT_CONVERTER.connect(git), conv_version, converterVersionLabel, convProgress);
    }

    private void checkVersion(GHRepository git, Version current, Label label, ProgressIndicator indicator) {
        if (current == null) return;
        GHRelease latest;
        try {
            latest = git != null? git.getLatestRelease() : null;
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Nie można połączyć się z repozytoriami!");
            a.show();
            return;
        }
        if (latest != null) {
            Version latestVersion = Version.parse(latest.getTagName());
            switch (current.compareTo(latestVersion)) {
                case 0: break;
                default:
                    label.setText('v' + current.toString() + " [v" + latestVersion.toString() + "]");
                    indicator.setDisable(false);
                    update.setDisable(false);
            }
        }
    }

    @FXML
    private void update() {
        GitHub git = GitHubConnection.get();
        if (basicProgress.isDisabled()) {
            SequenceService service = buildDownloadService(git, GitRepository.E_METRYKANT);
            if (service != null) {
                service.init();
                basicProgress.progressProperty().bind(service.progressProperty());
                service.setOnSucceeded(event1 -> {
                    if (service.hasSomeTask()) service.restart();
                    else basicProgress.setDisable(true);
                });
                service.start();
            }
        }
        if (convProgress.isDisabled()) {
            SequenceService service = buildDownloadService(git, GitRepository.E_METRYKANT_CONVERTER);
            if (service != null) {
                service.init();
                basicProgress.progressProperty().bind(service.progressProperty());
                service.setOnSucceeded(event1 -> {
                    if (service.hasSomeTask()) service.restart();
                    else convProgress.setDisable(true);
                });
                service.start();
            }
        }
        update.setDisable(true);
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
        SequenceService service = new SequenceService() {
            @Override
            public void init() {
                super.tasks.add(new DownloadFile(zipURL, Main.USER_DIR, zipNameContains + ".zip", zipSize));
                super.tasks.add(new ExtractZip(new File(Main.USER_DIR, zipNameContains + ".zip")));
            }
        };
        service.init();
        return service;
    }

    private SequenceService buildDownloadService(GitHub git, GitRepository repo) {
        return buildDownloadService(repo.connect(git), repo.getZipNameContains());
    }
}
