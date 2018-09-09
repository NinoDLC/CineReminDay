package fr.delcey.cinereminday.main.recyclerview_viewmodel;

import fr.delcey.cinereminday.main.CRDMainAdapter;

/**
 * Created by Nino on 09/09/2018.
 */

public class CRDMainViewModelAskSmsPermission extends CRDMainViewModel {

    @Override
    public int getItemType() {
        return CRDMainAdapter.ITEM_TYPE_ASK_SMS_PERMISSION;
    }
}
