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

import javafx.application.Platform;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Umożliwia stworzenie połączenia z GitHub.com. Klasa singleton.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
class GitHubConnection {

    private static GitHub connection = null;

    private GitHubConnection() {}

    private static GitHub createConnection() throws IOException {
        System.out.println("Creating new connection...");
        return new GitHubBuilder()
                .withRateLimitHandler(new RateLimitHandler() {
                    @Override
                    public void onError(IOException e, HttpURLConnection uc) {
                        e.printStackTrace();
                        System.out.println(uc);
                    }
                }).withOAuthToken("5c3210fa72227686dbaf630c6e192e68e7fae89c").build();
    }

    private static boolean isConnected() {
        return connection != null;
    }

    private static void tryConnect() {
        try {
            connection = createConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * Próbuje nawiązać połączenie z GitHub.com, gdy się to uda zwraca je.
     * @return nowe połączenie, gdy nie połączono wcześniej, albo wcześniejsze połąćzenie
     */
    static GitHub get() {
        while (!isConnected()) tryConnect();
        System.out.println("Connected!");
        return connection;
    }
}
