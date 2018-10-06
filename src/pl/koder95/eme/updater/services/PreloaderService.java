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

import pl.koder95.eme.updater.task.DownloadFile;
import pl.koder95.eme.updater.task.ExtractZip;

import java.io.File;

/**
 * Klasa rozszerza {@link pl.koder95.eme.updater.services.SequenceService} i umożliwia uruchomienie usługi,
 * która ma wykonać zadania dla Preloadera.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public class PreloaderService extends SequenceService {

    private DownloadFile downloadZip;
    private ExtractZip extractZip;

    /**
     * Tworzy nową usługę, która najpierw pobierze archiwum ZIP z URL, a później rozpakuje w określonym folderze.
     *
     * @param zipURL adres URL, pod którym znajduje się archiwum ZIP do pobrania
     * @param dir folder, gdzie zostanie wypakowane pobrane archiwum ZIP
     * @param zipName nazwa pliku, pod jaką ma zostać zapisane archiwum
     * @param zipSize rozmiar w bajtach (B) archiwum do pobrania
     */
    public PreloaderService(String zipURL, File dir, String zipName, long zipSize) {
        this.downloadZip = new DownloadFile(zipURL, dir, zipName, zipSize);
        this.extractZip = new ExtractZip(new File(dir, zipName));
    }

    @Override
    public void init() {
        tasks.add(downloadZip);
        tasks.add(extractZip);
    }
}
