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

package com.xuexiang.xupdate.widget;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.xuexiang.xupdate.R;
import com.xuexiang.xupdate._XUpdate;
import com.xuexiang.xupdate.entity.PromptEntity;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IPrompterProxy;
import com.xuexiang.xupdate.utils.ColorUtils;
import com.xuexiang.xupdate.utils.DialogUtils;
import com.xuexiang.xupdate.utils.DrawableUtils;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.io.File;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.DOWNLOAD_PERMISSION_DENIED;
import static com.xuexiang.xupdate.entity.UpdateError.ERROR.PROMPT_UNKNOWN;

/**
 * ????????????????????????DialogFragment?????????
 *
 * @author xuexiang
 * @since 2018/7/2 ??????11:40
 */
public class UpdateDialogFragment extends DialogFragment implements View.OnClickListener, IDownloadEventHandler {
    public final static String KEY_UPDATE_ENTITY = "key_update_entity";
    public final static String KEY_UPDATE_PROMPT_ENTITY = "key_update_prompt_entity";

    public final static int REQUEST_CODE_REQUEST_PERMISSIONS = 111;

    //======??????========//
    /**
     * ????????????
     */
    private ImageView mIvTop;
    /**
     * ??????
     */
    private TextView mTvTitle;
    //======????????????========//
    /**
     * ??????????????????
     */
    private TextView mTvUpdateInfo;
    /**
     * ????????????
     */
    private Button mBtnUpdate;
    /**
     * ????????????
     */
    private Button mBtnBackgroundUpdate;
    /**
     * ????????????
     */
    private TextView mTvIgnore;
    /**
     * ?????????
     */
    private NumberProgressBar mNumberProgressBar;
    //======??????========//
    /**
     * ????????????
     */
    private LinearLayout mLlClose;
    private ImageView mIvClose;

    //======????????????========//
    /**
     * ????????????
     */
    private UpdateEntity mUpdateEntity;
    /**
     * ????????????
     */
    private static IPrompterProxy sIPrompterProxy;
    /**
     * ?????????????????????
     */
    private PromptEntity mPromptEntity;
    /**
     * ??????????????????
     */
    private int mCurrentOrientation;

    /**
     * ??????????????????
     *
     * @param fragmentManager fragment?????????
     * @param updateEntity    ????????????
     * @param prompterProxy   ????????????
     * @param promptEntity    ?????????????????????
     */
    public static void show(@NonNull FragmentManager fragmentManager, @NonNull UpdateEntity updateEntity, @NonNull IPrompterProxy prompterProxy, @NonNull PromptEntity promptEntity) {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_UPDATE_ENTITY, updateEntity);
        args.putParcelable(KEY_UPDATE_PROMPT_ENTITY, promptEntity);
        fragment.setArguments(args);
        setIPrompterProxy(prompterProxy);
        fragment.show(fragmentManager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _XUpdate.setIsPrompterShow(getUrl(), true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.XUpdate_Fragment_Dialog);
        mCurrentOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    public void onStart() {
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        // ???super.onStart();?????????mDialog.show
        super.onStart();
        DialogUtils.syncSystemUiVisibility(getActivity(), window);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        initDialog();
    }

    private void initDialog() {
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // ????????????????????????????????????????????????
                return keyCode == KeyEvent.KEYCODE_BACK && mUpdateEntity != null && mUpdateEntity.isForce();
            }
        });
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        PromptEntity promptEntity = getPromptEntity();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (promptEntity.getWidthRatio() > 0 && promptEntity.getWidthRatio() < 1) {
            lp.width = (int) (displayMetrics.widthPixels * promptEntity.getWidthRatio());
        }
        if (promptEntity.getHeightRatio() > 0 && promptEntity.getHeightRatio() < 1) {
            lp.height = (int) (displayMetrics.heightPixels * promptEntity.getHeightRatio());
        }
        window.setAttributes(lp);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.xupdate_layout_update_prompter, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initView(View view) {
        // ????????????
        mIvTop = view.findViewById(R.id.iv_top);
        // ??????
        mTvTitle = view.findViewById(R.id.tv_title);
        // ????????????
        mTvUpdateInfo = view.findViewById(R.id.tv_update_info);
        // ????????????
        mBtnUpdate = view.findViewById(R.id.btn_update);
        // ??????????????????
        mBtnBackgroundUpdate = view.findViewById(R.id.btn_background_update);
        // ??????
        mTvIgnore = view.findViewById(R.id.tv_ignore);
        // ?????????
        mNumberProgressBar = view.findViewById(R.id.npb_progress);

        // ????????????+??? ???????????????
        mLlClose = view.findViewById(R.id.ll_close);
        // ????????????
        mIvClose = view.findViewById(R.id.iv_close);
    }

    /**
     * ???????????????
     */
    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mPromptEntity = bundle.getParcelable(KEY_UPDATE_PROMPT_ENTITY);
        // ???????????????
        if (mPromptEntity == null) {
            // ?????????????????????????????????
            mPromptEntity = new PromptEntity();
        }
        initTheme(mPromptEntity.getThemeColor(), mPromptEntity.getTopResId(), mPromptEntity.getButtonTextColor());
        mUpdateEntity = bundle.getParcelable(KEY_UPDATE_ENTITY);
        if (mUpdateEntity != null) {
            initUpdateInfo(mUpdateEntity);
            initListeners();
        }
    }

    /**
     * @return ?????????????????????????????????
     */
    private PromptEntity getPromptEntity() {
        // ??????bundle?????????
        if (mPromptEntity == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                mPromptEntity = bundle.getParcelable(KEY_UPDATE_PROMPT_ENTITY);
            }
        }
        // ????????????????????????????????????
        if (mPromptEntity == null) {
            mPromptEntity = new PromptEntity();
        }
        return mPromptEntity;
    }

    /**
     * ?????????????????????
     *
     * @param updateEntity ??????????????????
     */
    private void initUpdateInfo(UpdateEntity updateEntity) {
        // ???????????????
        final String newVersion = updateEntity.getVersionName();
        String updateInfo = UpdateUtils.getDisplayUpdateInfo(getContext(), updateEntity);
        // ????????????
        mTvUpdateInfo.setText(updateInfo);
        mTvTitle.setText(String.format(getString(R.string.xupdate_lab_ready_update), newVersion));

        // ????????????????????????
        refreshUpdateButton();
        // ????????????,?????????????????????
        if (updateEntity.isForce()) {
            mLlClose.setVisibility(View.GONE);
        }
    }

    /**
     * ??????????????????
     */
    private void initTheme(@ColorInt int themeColor, @DrawableRes int topResId, @ColorInt int buttonTextColor) {
        if (themeColor == -1) {
            themeColor = ColorUtils.getColor(getContext(), R.color.xupdate_default_theme_color);
        }
        if (topResId == -1) {
            topResId = R.drawable.xupdate_bg_app_top;
        }
        if (buttonTextColor == 0) {
            buttonTextColor = ColorUtils.isColorDark(themeColor) ? Color.WHITE : Color.BLACK;
        }
        setDialogTheme(themeColor, topResId, buttonTextColor);
    }

    /**
     * ??????
     *
     * @param themeColor ?????????
     * @param topResId   ??????
     */
    private void setDialogTheme(int themeColor, int topResId, int buttonTextColor) {
        Drawable topDrawable = _XUpdate.getTopDrawable(mPromptEntity.getTopDrawableTag());
        if (topDrawable != null) {
            mIvTop.setImageDrawable(topDrawable);
        } else {
            mIvTop.setImageResource(topResId);
        }
        DrawableUtils.setBackgroundCompat(mBtnUpdate, DrawableUtils.getDrawable(UpdateUtils.dip2px(4, getContext()), themeColor));
        DrawableUtils.setBackgroundCompat(mBtnBackgroundUpdate, DrawableUtils.getDrawable(UpdateUtils.dip2px(4, getContext()), themeColor));
        mNumberProgressBar.setProgressTextColor(themeColor);
        mNumberProgressBar.setReachedBarColor(themeColor);
        mBtnUpdate.setTextColor(buttonTextColor);
        mBtnBackgroundUpdate.setTextColor(buttonTextColor);
    }

    private void initListeners() {
        mBtnUpdate.setOnClickListener(this);
        mBtnBackgroundUpdate.setOnClickListener(this);
        mIvClose.setOnClickListener(this);
        mTvIgnore.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        // ?????????????????????????????????apk???
        if (i == R.id.btn_update) {
            // ???????????????????????????????????????????????????
            int flag = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (!UpdateUtils.isPrivateApkCacheDir(mUpdateEntity) && flag != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_REQUEST_PERMISSIONS);
            } else {
                installApp();
            }
        } else if (i == R.id.btn_background_update) {
            // ????????????????????????
            if (sIPrompterProxy != null) {
                sIPrompterProxy.backgroundDownload();
            }
            dismissDialog();
        } else if (i == R.id.iv_close) {
            // ??????????????????
            if (sIPrompterProxy != null) {
                sIPrompterProxy.cancelDownload();
            }
            dismissDialog();
        } else if (i == R.id.tv_ignore) {
            // ??????????????????
            UpdateUtils.saveIgnoreVersion(getActivity(), mUpdateEntity.getVersionName());
            dismissDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ??????
                installApp();
            } else {
                _XUpdate.onUpdateError(DOWNLOAD_PERMISSION_DENIED);
                dismissDialog();
            }
        }

    }

    private void installApp() {
        if (UpdateUtils.isApkDownloaded(mUpdateEntity)) {
            onInstallApk();
            // ???????????????
            // ??????????????????????????????????????????????????????????????????????????????????????????app?????????????????????????????????????????????????????????????????????
            if (!mUpdateEntity.isForce()) {
                dismissDialog();
            } else {
                showInstallButton();
            }
        } else {
            if (sIPrompterProxy != null) {
                sIPrompterProxy.startDownload(mUpdateEntity, new WeakFileDownloadListener(this));
            }
            // ??????????????????????????????????????????
            if (mUpdateEntity.isIgnorable()) {
                mTvIgnore.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void handleStart() {
        if (!UpdateDialogFragment.this.isRemoving()) {
            doStart();
        }
    }

    private void doStart() {
        mNumberProgressBar.setVisibility(View.VISIBLE);
        mNumberProgressBar.setProgress(0);
        mBtnUpdate.setVisibility(View.GONE);
        if (mPromptEntity.isSupportBackgroundUpdate()) {
            mBtnBackgroundUpdate.setVisibility(View.VISIBLE);
        } else {
            mBtnBackgroundUpdate.setVisibility(View.GONE);
        }
    }

    @Override
    public void handleProgress(float progress) {
        if (!UpdateDialogFragment.this.isRemoving()) {
            if (mNumberProgressBar.getVisibility() == View.GONE) {
                doStart();
            }
            mNumberProgressBar.setProgress(Math.round(progress * 100));
            mNumberProgressBar.setMax(100);
        }
    }

    @Override
    public boolean handleCompleted(File file) {
        if (!UpdateDialogFragment.this.isRemoving()) {
            mBtnBackgroundUpdate.setVisibility(View.GONE);
            if (mUpdateEntity.isForce()) {
                showInstallButton();
            } else {
                dismissDialog();
            }
        }
        // ??????true???????????????apk??????
        return true;
    }

    @Override
    public void handleError(Throwable throwable) {
        if (!UpdateDialogFragment.this.isRemoving()) {
            if (mPromptEntity.isIgnoreDownloadError()) {
                refreshUpdateButton();
            } else {
                dismissDialog();
            }
        }
    }

    /**
     * ????????????????????????
     */
    private void refreshUpdateButton() {
        if (UpdateUtils.isApkDownloaded(mUpdateEntity)) {
            showInstallButton();
        } else {
            showUpdateButton();
        }
        mTvIgnore.setVisibility(mUpdateEntity.isIgnorable() ? View.VISIBLE : View.GONE);
    }

    /**
     * ?????????????????????
     */
    private void showInstallButton() {
        mNumberProgressBar.setVisibility(View.GONE);
        mBtnBackgroundUpdate.setVisibility(View.GONE);
        mBtnUpdate.setText(R.string.xupdate_lab_install);
        mBtnUpdate.setVisibility(View.VISIBLE);
        mBtnUpdate.setOnClickListener(this);
    }

    /**
     * ?????????????????????
     */
    private void showUpdateButton() {
        mNumberProgressBar.setVisibility(View.GONE);
        mBtnBackgroundUpdate.setVisibility(View.GONE);
        mBtnUpdate.setText(R.string.xupdate_lab_update);
        mBtnUpdate.setVisibility(View.VISIBLE);
        mBtnUpdate.setOnClickListener(this);
    }

    private void onInstallApk() {
        _XUpdate.startInstallApk(getContext(), UpdateUtils.getApkFileByUpdateEntity(mUpdateEntity), mUpdateEntity.getDownLoadEntity());
    }

    /**
     * ????????????
     */
    private void dismissDialog() {
        _XUpdate.setIsPrompterShow(getUrl(), false);
        clearIPrompterProxy();
        dismissAllowingStateLoss();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (manager.isDestroyed() || manager.isStateSaved()) {
                return;
            }
        }
        try {
            super.show(manager, tag);
        } catch (Exception e) {
            _XUpdate.onUpdateError(PROMPT_UNKNOWN, e.getMessage());
        }
    }

    /**
     * ??????????????????
     *
     * @param manager ?????????
     */
    public void show(FragmentManager manager) {
        show(manager, "update_dialog");
    }

    @Override
    public void onDestroyView() {
        _XUpdate.setIsPrompterShow(getUrl(), false);
        clearIPrompterProxy();
        super.onDestroyView();
    }

    private static void setIPrompterProxy(IPrompterProxy prompterProxy) {
        UpdateDialogFragment.sIPrompterProxy = prompterProxy;
    }

    private static void clearIPrompterProxy() {
        if (sIPrompterProxy != null) {
            sIPrompterProxy.recycle();
            sIPrompterProxy = null;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mCurrentOrientation) {
            reloadView();
        }
        mCurrentOrientation = newConfig.orientation;
    }

    private void reloadView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.xupdate_layout_update_prompter, null);
        ViewGroup root = (ViewGroup) getView();
        if (root != null) {
            root.removeAllViews();
            root.addView(view);
            initView(root);
            initData();
        }
    }

    private String getUrl() {
        return sIPrompterProxy != null ? sIPrompterProxy.getUrl() : "";
    }

}

