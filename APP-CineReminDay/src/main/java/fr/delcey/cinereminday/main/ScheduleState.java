package fr.delcey.cinereminday.main;

public enum ScheduleState {
    // This is the nominal "longest" state : it goes (in nominal cases) from tuesday 8:11 AM to 8:10 AM the next tuesday
    WAITING_FOR_ALARM_MANAGER,
    
    // Alarm Manager woke the application, we sent the SMS
    WAITING_FOR_SMS_RESPONSE
}
