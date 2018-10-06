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
package pl.koder95.eme.updater.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Klasa rozszerza {@link javafx.concurrent.Service} i umożliwia uruchomienie usługi,
 * która wykona kilka zadań po kolei.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public class SequenceService extends Service {

    /**
     * Kolejka zadań. W jakiej kolejności zostaną dodane, w takiej również uruchomione.
     */
    protected final Queue<Task> tasks = new LinkedList<>();

    /**
     * Służy do określania kolejki zadań.
     */
    public void init() {}

    @Override
    protected Task createTask() {
        return tasks.poll();
    }

    /**
     * Sprawdza, czy jest jakieś zadanie do wykonania.
     *
     * @return {@code true} - jest przynajmniej jedno zadanie do wykonania,
     * w przeciwnym wypadku {@code false}
     */
    public boolean hasSomeTask() {
        return !tasks.isEmpty();
    }

    @Override
    public void restart() {
        if (hasSomeTask()) super.restart();
    }
}
