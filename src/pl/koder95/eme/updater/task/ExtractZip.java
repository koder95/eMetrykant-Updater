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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Klasa rozszerza {@link javafx.concurrent.Task} umożliwiając rozpakowanie archiwum ZIP śledząc postęp
 * tego procesu.
 *
 * @author Kamil Jan Mularski [@koder95]
 * @version 1.0.0, 2018-10-06
 * @since 1.0.0
 */
public class ExtractZip extends Task {

    private final File forExtract;
    private final File dir;
    private final boolean deleteZipLater;

    private ExtractZip(File forExtract, File dir, boolean deleteZipLater) {
        this.forExtract = forExtract;
        this.dir = dir;
        this.deleteZipLater = deleteZipLater;
    }

    private ExtractZip(File forExtract, File dir) {
        this(forExtract, dir, true);
    }

    private ExtractZip(File forExtract, boolean deleteZipLater) {
        this(forExtract, null, deleteZipLater);
    }

    /**
     * Tworzy nowe zadanie rozpakowania ZIP. Rozpakowuje w tym samym miejscu, gdzie znajduje się
     * archiwum do rozpakowywania.
     * @param forExtract archiwum ZIP, które ma zostać rozpakowane
     */
    public ExtractZip(File forExtract) {
        this(forExtract, true);
    }

    @Override
    protected Boolean call() throws Exception {
        updateProgress(0, 1);
        updateTitle("Rozpakowywanie " + forExtract.getName());
        updateMessage("");
        try (ZipFile zip = new ZipFile(forExtract)) {
            updateProgress(0, forExtract.length());
            System.out.println(zip.getName() + " +=> " + forExtract.length());
            double total = 0;
            System.out.println("Total: 0");
            File dir = this.dir;
            if (dir == null) dir = forExtract.getParentFile();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println(entry);
                if (entry.isDirectory()) {
                    System.out.println(entry + " is directory.");
                    if (new File(dir, entry.getName()).mkdirs()) System.out.println("Directory created!");
                }

                InputStream input = zip.getInputStream(entry);
                int available = input.available();
                System.out.println(entry.getName() + " +=> " + available);
                total += available;
                System.out.println("Total: " + total);
            }
            entries = zip.entries();
            double work = 0;
            System.out.println("Work: 0");
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File outFile = new File(dir, entry.getName());

                if (!entry.isDirectory()) {
                    if (outFile.createNewFile()) {
                        System.out.println("New file created: " + outFile);
                    }
                }
                else continue;

                System.out.println("Entry: " + entry);
                InputStream input = zip.getInputStream(entry);
                try (FileOutputStream output = new FileOutputStream(outFile)) {
                    while (input.available() > 0) {
                        int b = input.read();
                        System.out.println("Read: " + b);
                        output.write(b);
                        output.flush();
                        updateProgress(++work, total);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
        updateProgress(Double.NaN, 0);
        if (deleteZipLater) return forExtract.delete();
        return false;
    }
}
