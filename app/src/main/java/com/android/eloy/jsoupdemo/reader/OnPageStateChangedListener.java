package com.android.eloy.jsoupdemo.reader;

import com.android.eloy.jsoupdemo.reader.response.Chapter;

import java.util.List;

public interface OnPageStateChangedListener {

    void onCenterClick();

    void onChapterChanged(int currentChapter, int fromChapter, boolean fromUser);

    void onPageChanged(int currentPage, int currentChapter);

    void onChapterLoadFailure(int targetChapter, int currentChapter, List<Chapter> chapters);
}
