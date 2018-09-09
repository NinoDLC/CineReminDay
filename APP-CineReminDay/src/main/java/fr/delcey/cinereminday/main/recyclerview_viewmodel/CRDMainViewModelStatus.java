package fr.delcey.cinereminday.main.recyclerview_viewmodel;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import fr.delcey.cinereminday.main.CRDMainAdapter;

/**
 * Created by Nino on 09/09/2018.
 */

public class CRDMainViewModelStatus extends CRDMainViewModel {

    @DrawableRes
    private int mIconRes;
    @StringRes
    private int mTitleRes;
    private String mMessage;

    public CRDMainViewModelStatus(@DrawableRes int iconRes, @StringRes int titleRes, String message) {
        mIconRes = iconRes;
        mTitleRes = titleRes;
        mMessage = message;
    }

    @DrawableRes
    public int getIconRes() {
        return mIconRes;
    }

    @StringRes
    public int getTitleRes() {
        return mTitleRes;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public int getItemType() {
        return CRDMainAdapter.ITEM_TYPE_STATUS;
    }
}
