package fr.delcey.cinereminday.main.recyclerview_viewmodel;

/**
 * Created by Nino on 09/09/2018.
 */

public abstract class CRDMainViewModel {

    public static final int ITEM_TYPE_STATUS = 0;
    public static final int ITEM_TYPE_ASK_SMS_PERMISSION = 1;
    public static final int ITEM_TYPE_ASK_SMS_SCHEDULE = 2;

    public abstract int getItemType();
}
