package com.softwaretesting.adapters.ui.screen;

import com.softwaretesting.adapters.ui.LibGdxApplication;
import com.softwaretesting.adapters.ui.dto.UserStatisticsDTO;
import com.softwaretesting.adapters.ui.view.StatisticsView;

import java.util.List;

public class StatisticsScreen extends ScreenTemplate implements StatisticsView {

    public StatisticsScreen(LibGdxApplication application, Long id) {
        super(application);
    }

    @Override
    public void displayUserStatistics(List<UserStatisticsDTO> userStats) {

    }

    @Override
    public void displayOverallStatistics(int totalSimulations, float avgSuccessPerUser, float overallAvgSuccess) {

    }

    @Override
    public void navigateBack() {

    }
}
