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

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Klasa podstawowa dla programu, rozszerza {@link javafx.application.Application}.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.1, 2018-10-09
 * @since 1.0.0
 */
public class Main extends Application {

    /**
     * Folder, gdzie znajdują się pliki programu.
     */
    static final File USER_DIR = new File(System.getProperties().getProperty("user.dir"));
    /**
     * Plik uruchomieniowy <i>eMetrykant.jar</i>.
     */
    static final File EMETRYKANT_JAR = new File(USER_DIR, "eMetrykant.jar");
    /**
     * Plik uruchomieniowy <i>Converter.jar</i>.
     */
    static final File EMETRYKANT_CONV_JAR = new File(USER_DIR, "Converter.jar");

    private HashMap<String, Object> resources = new HashMap<>();
    private Parent root;
    
    @Override
    public void init() throws Exception {
        System.out.println("Main#init");
        resources.put("eMetrykant.jar JarAnalyzer", new JarAnalyzer(EMETRYKANT_JAR));
        resources.put("Converter.jar JarAnalyzer", new JarAnalyzer(EMETRYKANT_CONV_JAR));
        root = FXMLLoader.load(Objects.requireNonNull(FXMLLoader.getDefaultClassLoader()
                .getResource("pl/koder95/eme/updater/FXML.fxml")), new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return resources.get(key);
            }

            @Override
            public Enumeration<String> getKeys() {
                return new Enumeration<String>() {
                    private Iterator<String> iterator = resources.keySet().iterator();

                    @Override
                    public boolean hasMoreElements() {
                        return iterator.hasNext();
                    }

                    @Override
                    public String nextElement() {
                        return iterator.next();
                    }
                };
            }
        });
    }
    
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("eMetrykant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("eMetrykant Updater v" + Version.get());
        if (args == null || args.length == 0)
            LauncherImpl.launchApplication(Main.class, Preloader.class, new String[0]);
        else if (args[0].equals("-r")) restart();
    }

    public static void restart() {
        Platform.exit();
        try {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            File self = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(javaBin, "-jar", self.getName());
            builder.start();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    
}
