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

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Udostępnia repozytoria skąd można pobrać pliki.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public enum GitRepository {
    E_METRYKANT("eMetrykant"), E_METRYKANT_CONVERTER("eMetrykant-Converter", "Converter"),
    E_METRYKANT_UPDATER("eMetrykant-Updater", "Updater");

    private final GitUser user;
    private final String name;
    private final String zipNameContains;

    GitRepository(GitUser user, String name, String zipNameContains) {
        this.user = user;
        this.name = name;
        this.zipNameContains = zipNameContains;
    }

    GitRepository(String name, String zipNameContains) {
        this(GitUser.DEFAULT, name, zipNameContains);
    }
    GitRepository(String name) {
        this(name, name);
    }

    /**
     * Pobiera informacje o repozytorium wykorzystując połączenie HTTP.
     *
     * @return repozytorium GitHub, chyba że repozytorium nie istnieje, wtedy {@code null}
     */
    public GHRepository connect(GitHub connection) {
        GHUser user = this.user.connect(connection);
        if (user == null) return null;
        try {
            if (user.getRepositories().containsKey(name)) return user.getRepository(name);
            else return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return ciąg, który musi zawierać plik Zip, aby rozpoznać go spośród innych
     */
    public String getZipNameContains() {
        return zipNameContains;
    }

    /**
     * @return nazwa repozytorium GitHub
     */
    public String getName() {
        return name;
    }
}
