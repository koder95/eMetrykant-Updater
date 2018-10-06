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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Klasa umożliwia w łatwy sposób zarządzać plikiem JAR
 * na potrzeby aplikacji.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public class JarAnalyzer {

    private final File jar;

    JarAnalyzer(File jar) {
        this.jar = jar;
    }

    private Class<?> getVersionClass() {
        if (!jar.exists()) return null;
        JarEntry versionEntry = null;
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith("Version.class")) {
                    if (versionEntry != null) {
                        if (versionEntry.getName().length() > entry.getName().length())
                            versionEntry = entry;
                    }
                    else versionEntry = entry;
                }
            }
        } catch (IOException ex) {
            System.err.println("Nie można odczytać pliku.");
        }
        if (versionEntry == null) return null;

        try {
            URLClassLoader ucl = new URLClassLoader(new URL[] { jar.toURI().toURL() });
            return ucl.loadClass(versionEntry.getName().substring(0, versionEntry.getName().lastIndexOf('.'))
                    .replaceAll("/", "."));
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Pobiera klasę wersji z archiwum JAR i zwraca obiekt utworzony za pomocą statycznej metody {@code get()}.
     *
     * @return null - jeśli archiwum JAR nie zawiera klasy wersji lub klasa nie posiada właściwej metody {@code get()},
     * lub w przeciwnym razie zwraca obiekt zawierający informacje o wersji
     */
    public Object getVersion() {
        try {
            Class<?> klasa = getVersionClass();
            if (klasa == null) return null;
            return klasa.getMethod("get").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }
}
