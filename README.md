# CineReminDay
Features : 
* Schedule Cineday code collect on Tuesday morning (sending a SMS to a free service for Orange users)
* Share and query valid Cineday codes to other users through Firebase Database
* Share code to a friend
* Dashboard displays every state of the app, depending on time / state of SMS / code sharing / etc...

# Use case : 
1. User launch app
2. User grants SMS sending and receiving permission
3. User review the content of the message to be sent (required from Google Play because of the Spam policies)
4. AlarmManager is scheduled for the next Tuesday morning, 8:10 AM, exactly. Information is displayed to user through dashboard
5. On Tuesday morning, 8:10AM, an SMS is sent automatically to the Orange Cineday service (number : 20000) to get a Cineday code
6. When SMS is sent, app starts to analyze incoming SMS, working only on SMS coming from the Orange Cineday service
7. Once Orange Cineday answer SMS is received, app stops analyzing incoming SMS, and display on dashboard the result of the SMS "query" (either a Cineday code or an error if the limit has been reached)
8. Code is valid until the end of the day, can be shareable with a friend (won't make the code disappear from dashboard) or with community
9. At the end of the day, goes back to 4/

Optional :
* 5a. On tuesdays, any user can ask the community for a code, going to 7/
* 8a. If code is shared with community, the code disappear from user dashboard and is given to the pool of available codes on Firebase, and he can ask for a new code (5a/)
