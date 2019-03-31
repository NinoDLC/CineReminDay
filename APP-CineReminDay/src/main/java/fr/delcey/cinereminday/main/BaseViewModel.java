package fr.delcey.cinereminday.main;

import androidx.lifecycle.ViewModel;
import fr.delcey.cinereminday.RemindayUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseViewModel extends ViewModel {

    protected List<BaseUseCase> useCaseList = new ArrayList<>();

    public BaseViewModel(BaseUseCase... useCases) {
        if(!RemindayUtils.isArrayEmpty(useCases)) {
            useCaseList.addAll(Arrays.asList(useCases));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        for (BaseUseCase baseUseCase : useCaseList) {
            baseUseCase.cancel();
        }
    }
}
