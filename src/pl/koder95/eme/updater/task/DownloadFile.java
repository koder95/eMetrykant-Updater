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
package pl.koder95.eme.updater.task;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.NumberFormat;

/**
 * Klasa rozszerza {@link javafx.concurrent.Task} i umożliwia pobranie
 * do określonego folderu archiwum ZIP, śledząc proces pobierania.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.1, 2018-10-09
 * @since 1.0.0
 */
public class DownloadFile extends Task<File> {

    private String url;
    private File dir;
    private String name;
    private long size;

    /**
     * Tworzy nowe zadanie pobrania plik z określonego URL i zapisania w określonym folderze
     * pod podaną nazwą pliku.
     *
     * @param url adres URL, pod którym znajduje się plik do pobrania
     * @param dir folder, gdzie zostanie umieszczony pobrany plik
     * @param name nazwa pliku, pod jaką ma zostać zapisany plik
     * @param size rozmiar w bajtach (B) pliku do pobrania
     */
    public DownloadFile(String url, File dir, String name, long size) {
        this.url = url;
        this.dir = dir;
        this.name = name;
        this.size = size;
    }

    @Override
    protected File call() throws Exception {
        NumberFormat pF = NumberFormat.getPercentInstance();
        String title = "Pobieranie " + name;
        updateTitle(title);
        updateProgress(0, 1);

        URLConnection connection = new URL(url).openConnection();
        File forDownload = new File(dir, name);

        try (InputStream in = connection.getInputStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream out = new FileOutputStream(forDownload);
             FileChannel channel = out.getChannel()) {
            channel.truncate(size);
            in.mark((int) size);
            long totalWork = size;
            long workDone = 0;
            long count = totalWork > 100? totalWork / 100: 1;
            while (workDone < totalWork) {
                long transferred = channel.transferFrom(rbc, workDone, count);
                workDone += transferred;
                updateMessage(pF.format((double) workDone/totalWork));
                updateProgress(workDone, totalWork);
            }
            channel.force(true);
        } catch (Exception ex) { ex.printStackTrace(); }

        return forDownload;
    }
}
