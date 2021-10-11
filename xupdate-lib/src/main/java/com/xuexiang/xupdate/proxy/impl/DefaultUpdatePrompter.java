/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.xupdate.proxy.impl;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.xuexiang.xupdate.entity.PromptEntity;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IUpdatePrompter;
import com.xuexiang.xupdate.proxy.IUpdateProxy;
import com.xuexiang.xupdate.widget.UpdateDialog;
import com.xuexiang.xupdate.widget.UpdateDialogFragment;

/**
 * 默认的更新提示器
 *
 * @author xuexiang
 * @since 2018/7/2 下午4:05
 */
public class DefaultUpdatePrompter implements IUpdatePrompter {

    private FragmentManager mFragmentManager;

    /**
     * 使用默认Dialog
     */
    public DefaultUpdatePrompter() {
    }

    /**
     * 使用FragmentDialog
     *
     * @param manager
     */
    public DefaultUpdatePrompter(@NonNull FragmentManager manager) {
        mFragmentManager = manager;
    }

    /**
     * 显示版本更新提示
     *
     * @param updateEntity 更新信息
     * @param updateProxy  更新代理
     * @param promptEntity 提示界面参数
     */
    @Override
    public void showPrompt(@NonNull UpdateEntity updateEntity, @NonNull IUpdateProxy updateProxy, @NonNull PromptEntity promptEntity) {
        if (mFragmentManager != null) {
            UpdateDialogFragment.newInstance(updateEntity, updateProxy, promptEntity)
                    .show(mFragmentManager);
        } else {
            UpdateDialog.newInstance(updateEntity, updateProxy, promptEntity)
                    .show();
        }
    }
}
