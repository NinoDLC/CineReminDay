package fr.delcey.cinereminday.main;

public enum SmsCodeState {
    // Yay ! :)
    RECEIVED_SMS_CODE,
    
    // Nooo... No more code available :(
    // TODO VOLKO MAYBE PUT A DIFFERENCE BETWEEN NO MORE CODE AND "NOT YET OPENED" ?
    ERROR_SMS_CODE,
    
    // It's 8:10 AM but no cellular network... Patience, your time will come, little SMS !
    WAITING_FOR_CELLULAR_NETWORK
}
