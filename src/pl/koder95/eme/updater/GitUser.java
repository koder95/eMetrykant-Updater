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

import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Udostępnia użytkowników, z którymi można połączyć się, w celu pobrania plików.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public enum GitUser {
    /**
     * Domyślny użytkownik udostępniający pliki eMetrykanta.
     */
    DEFAULT("koder95");

    private final String login;

    GitUser(String login) {
        this.login = login;
    }

    /**
     * Pobiera informacje o użytkowniku wykorzystując połączenie HTTP.
     *
     * @param connection połączenie z GitHub za pomocą HTTP
     * @return użytkownik GitHub, chyba że {@code connection = null}, wtedy {@code null}
     */
    public GHUser connect(GitHub connection) {
        if (connection == null) return null;
        try {
            if (!connection.isOffline()) {
                System.out.println("Connection is online.");
                GHUser user = connection.getUser(login);
                System.out.println(user);
                return user;
            }
            else return connect(GitHub.connectAnonymously());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
