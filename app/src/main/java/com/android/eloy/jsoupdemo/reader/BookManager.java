package com.android.eloy.jsoupdemo.reader;

import android.text.TextUtils;

import com.android.eloy.jsoupdemo.reader.util.FileIOUtils;
import com.android.eloy.jsoupdemo.reader.util.FileUtils;

import java.io.File;

/**
 * Created by yuyuhang on 2018/1/11.
 */
public class BookManager {

    private static BookManager instance;

    public static BookManager getInstance() {
        if (instance == null) {
            synchronized (BookManager.class) {
                instance = new BookManager();
            }
        }
        return instance;
    }

    public String getBookNum(String title, String author) {
        StringBuilder result = new StringBuilder();
        if (!TextUtils.isEmpty(title)) {
            result.append(title.trim());
        }
        result.append("||");
        if (!TextUtils.isEmpty(author)) {
            result.append(author.trim());
        }
        return result.toString();
    }

    public File getContentFile(String sourceKey, String bookNum, String chapterName) {
        String path = FileUtils.createBookRootPath() + File.separator
                + sourceKey + File.separator
                + bookNum + File.separator + chapterName;
        File file = new File(path);
        if (!file.exists()) {
            FileUtils.createFile(file);
        }

        return file;
    }

    public boolean saveContentFile(String sourceKey, String bookNum, String chapterName, String content) {
        File file = getContentFile(sourceKey, bookNum, chapterName);
        return FileIOUtils.writeFileFromString(file, content);
    }
}
