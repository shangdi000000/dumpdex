package com.dump.listener;

import com.dump.bean.ApkResult;

import java.util.List;

/**
 * Created by huan on 2018/6/1.
 */

public interface DumpListener {
    void dumpSuccess(List<ApkResult> it);
    void dumpFailure(Throwable it);
}
