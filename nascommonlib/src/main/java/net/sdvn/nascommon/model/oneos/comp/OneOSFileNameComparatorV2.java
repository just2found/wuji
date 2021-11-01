package net.sdvn.nascommon.model.oneos.comp;

import android.text.TextUtils;

import net.sdvn.nascommon.model.oneos.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileNameComparatorV2 implements Comparator<OneOSFile> {
    private boolean mIsAsc;

    public OneOSFileNameComparatorV2(boolean isAsc) {
        mIsAsc = isAsc;
    }

    @Override
    public int compare(OneOSFile file1, OneOSFile file2) {
        if (file1 == null || file2 == null) {
            return 0;
        }
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        }

        if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }
        if (mIsAsc) {
            if (TextUtils.isEmpty(file1.getName()) || TextUtils.isEmpty(file1.getName()))
                return 0;
            return file1.getName().compareTo(file2.getName());
        } else {
            if (TextUtils.isEmpty(file1.getName()) || TextUtils.isEmpty(file1.getName()))
                return 0;
            return file2.getName().compareTo(file1.getName());

        }
    }
}