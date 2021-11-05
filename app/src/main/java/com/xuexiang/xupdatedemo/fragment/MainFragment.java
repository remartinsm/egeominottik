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

package com.xuexiang.xupdatedemo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xaop.consts.PermissionConsts;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageSimpleListFragment;
import com.xuexiang.xpage.utils.TitleBar;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate._XUpdate;
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xupdatedemo.R;
import com.xuexiang.xupdatedemo.activity.UpdateActivity;
import com.xuexiang.xupdatedemo.custom.CustomUpdateParser;
import com.xuexiang.xupdatedemo.custom.CustomUpdatePrompter;
import com.xuexiang.xupdatedemo.http.XHttpUpdateHttpService;
import com.xuexiang.xupdatedemo.utils.CProgressDialogUtils;
import com.xuexiang.xupdatedemo.utils.HProgressDialogUtils;
import com.xuexiang.xutil.app.IntentUtils;
import com.xuexiang.xutil.app.PathUtils;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.file.FileUtils;
import com.xuexiang.xutil.resource.ResUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * @author xuexiang
 * @since 2018/7/9 下午2:20
 */
@Page(name = "XUpdate 版本更新")
public class MainFragment extends XPageSimpleListFragment {

    private String mUpdateUrl = "https://raw.githubusercontent.com/xuexiangjys/XUpdate/master/jsonapi/update_test.json";

    private String mUpdateUrl2 = "https://raw.githubusercontent.com/xuexiangjys/XUpdate/master/jsonapi/update_forced.json";

    private String mUpdateUrl3 = "https://raw.githubusercontent.com/xuexiangjys/XUpdate/master/jsonapi/update_custom.json";

    private String mDownloadUrl = "https://raw.githubusercontent.com/xuexiangjys/XUpdate/master/apk/xupdate_demo_1.0.2.apk";

    private final static int REQUEST_CODE_SELECT_APK_FILE = 1000;
    @Override
    protected List<String> initSimpleData(List<String> lists) {
        lists.add("默认App更新");
        lists.add("默认App更新 + 支持后台更新");
        lists.add("版本更新(自动模式)");
        lists.add("强制版本更新");
        lists.add("默认App更新 + 自定义提示弹窗主题");
        lists.add("默认App更新 + 自定义Api");
        lists.add("默认App更新 + 自定义Api + 自定义提示弹窗(系统）");
        lists.add("使用apk下载功能");
        lists.add("使用apk安装功能");
        lists.add("版本更新提示框在FragmentActivity中使用UpdateDialogFragment, 在普通Activity中使用UpdateDialog");
        lists.add("使用XUpdateService版本更新服务");
        return lists;
    }

    @Override
    protected void onItemClick(int position) {
        switch (position) {
            case 0:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl)
                        .update();
                break;
            case 1:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl)
                        .supportBackgroundUpdate(true)
                        .update();
                break;
            case 2:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl)
                        .isAutoMode(true) //如果需要完全无人干预，自动更新，需要root权限【静默安装需要】
                        .update();
                break;
            case 3:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl2)
                        .update();
                break;
            case 4:
                XUpdate.newBuild(getActivity())
                        .updateHttpService(new XHttpUpdateHttpService("https://raw.githubusercontent.com"))
                        .updateUrl("/xuexiangjys/XUpdate/master/jsonapi/update_test.json")
                        .themeColor(ResUtils.getColor(R.color.update_theme_color))
                        .topResId(R.mipmap.bg_update_top)
                        .update();
                break;
            case 5:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl3)
                        .updateParser(new CustomUpdateParser())
                        .update();
                break;
            case 6:
                XUpdate.newBuild(getActivity())
                        .updateUrl(mUpdateUrl3)
                        .updateChecker(new DefaultUpdateChecker() {
                            @Override
                            public void onBeforeCheck() {
                                super.onBeforeCheck();
                                CProgressDialogUtils.showProgressDialog(getActivity(), "查询中...");
                            }
                            @Override
                            public void onAfterCheck() {
                                super.onAfterCheck();
                                CProgressDialogUtils.cancelProgressDialog(getActivity());
                            }
                        })
                        .updateParser(new CustomUpdateParser())
                        .updatePrompter(new CustomUpdatePrompter(getActivity()))
                        .update();
                break;
            case 7:
                useApkDownLoadFunction();
                break;
            case 8:
                selectAPKFile();
                break;
            case 9:
                startActivity(new Intent(getContext(), UpdateActivity.class));
                break;
            case 10:
                openPage(XUpdateServiceFragment.class);
                break;
            default:
                break;
        }
    }


    @Permission(PermissionConsts.STORAGE)
    private void useApkDownLoadFunction() {
        XUpdate.newBuild(getActivity())
                .apkCacheDir(PathUtils.getExtDownloadsPath())
                .build()
                .download(mDownloadUrl, new OnFileDownloadListener() {
                    @Override
                    public void onStart() {
                        HProgressDialogUtils.showHorizontalProgressDialog(getContext(), "下载进度", false);
                    }

                    @Override
                    public void onProgress(float progress, long total) {
                        HProgressDialogUtils.setProgress(Math.round(progress * 100));
                    }

                    @Override
                    public boolean onCompleted(File file) {
                        HProgressDialogUtils.cancel();
                        ToastUtils.toast("apk下载完毕，文件路径：" + file.getPath());
                        return false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        HProgressDialogUtils.cancel();
                    }
                });
    }

    @Permission(PermissionConsts.STORAGE)
    private void selectAPKFile() {
        startActivityForResult(IntentUtils.getDocumentPickerIntent(IntentUtils.DocumentType.ANY), REQUEST_CODE_SELECT_APK_FILE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_APK_FILE) {
                _XUpdate.startInstallApk(getContext(), FileUtils.getFileByPath(PathUtils.getFilePathByUri(getContext(), data.getData())));
            }
        }
    }



    @Override
    protected TitleBar initTitleBar() {
        return super.initTitleBar().setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickUtils.exitBy2Click();
            }
        });
    }


    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click();
        }
        return true;
    }
}
