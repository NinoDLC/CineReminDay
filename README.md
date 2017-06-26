# CineReminDay
Features : 
* Schedule Cineday code collect on Tuesday morning (sending a SMS to a free service for Orange users)
* Share and query valid Cineday codes to other users through Firebase Database
* Share code to a friend through SMS
* Dashboard displays every state of the app, depending on time / state of SMS / code sharing / etc...

# Use case : 
1. User launch app
2. User grants SMS sending and receiving permission
3. AlarmManager is scheduled for the next Tuesday morning, 8:10 AM, exactly. Information is displayed to user through dashboard
4. On Tuesday morning, 8:10AM, an SMS is sent automatically to the Orange Cineday service (number : 20000) to get a Cineday code
5. When SMS is sent, app starts to analyze incoming SMS, working only on SMS coming from the Orange Cineday service
6. Once Orange Cineday answer SMS is received, app stops analyzing incoming SMS, and display on dashboard the result of the SMS "query" (either a Cineday code or an error if the limit has been reached)
7. Code is valid until the end of Tuesday, can be shareable with a friend (won't make the code disappear from dashboard) or with community
8. At the end of the day, goes back to 3/

Optional :
* 4a. On tuesday, any user can ask the community for a code, going to 7/
* 7a. If code is shared with community, the code disappear from user dashboard and is given to the pool of available codes on Firebase, and he can ask for a new code (4a/)
