# A Sweet Cars and Bids App (as of 9/7/2020)
If [Cars & Bids](https://carsandbids.com/) were to have an Android app, this would be it.


I am a big fan of Doug Demuro. He is a car reviewer on YouTube I have been watching for at least six years now. He just launched a company called Cars & Bids, a car auction website for cool enthusiast cars, as well as quirky ones. Currently, there is no Android app for it on the Google Play store and I want to be an Android developer, so I thought what a perfect side project! This app allows me to work with a long time hobby of mine: learning cars and the auto industry, and do something I love: making apps! So, this is the app that maybe someday Cars & Bids will use for their company. Fingers crossed.

This app follows the MVVM Android architecture and is coded all in Kotlin. It uses Firebase Storage to hold the images of the cars, and Firebase Firestore to hold all other car data. All UI components were heavily influenced by the Cars & Bids website. Below, I have sectioned off different part of this README so you, the reader, can easily see what the app is like without needing to download the code yourself.

## The Auctions Page

This is the page users will first see when they open the app. This is where the many car auctions are listed, user can selected a listing to view a more detailed page to see more information about the car and seller, and bid on the car. 

Each listing shows a picture of the outside of the car, the time until the auction closes, the current bid, a sentence summarizing some cool features or mods of the car, and it's location. 

![AuctionsGif](readmegifs/auctionPageGif.gif)

## The Sell A Car Page

If the user would like to submit their vehicle to be reviewed for acceptance to be sold on Cars & Bids, this is the form they fill out. It is all basic and necessary info that will be needed, if the car is to be auctioned on Cars & Bids. Users can select photos from their phone's gallery to be uploaded, or take a photo on their camera through the app. The photo array submitted can be edited whenever the user likes before submission. All needed fields have validation checks applied to them and will show errors to the user where more and or proper info is still needed. 

The GIF below does not show all the abilities of the Sell a Car Page Fragment, but there are many views that are not seen until the user selects certain buttons. As to shorten the length of the GIF I only opened the "Dealer Information" views, but there are many more that will show other textboxes (EditText), or drop down menus (Spinners). When in the "Photos" section of the form, if user selects "Select Photo" or "Take Photo" first permissions are asked of the user if the app can have access to read and or write to files and access the camera. Once the user selects a photo or takes a picture, the photo is shown in the white box under "Photos." Beneath each photo is a delete button to remove that particular photo. If there are any photos that have been selected a third button appears below the white photo box labled "Clear All" and this will delete all photos from the photo box and from the photo array that would be submitted. 

![SellCarGif](readmegifs/sellACarPage.gif)

## The About Page

Here is where most questions can be answered about what Cars and Bids is and how it functions. The top half of the page the "what" of the about page, meaning it is where users can read basic questions about what Cars & Bids is. The second half of the page is the "how," meaning this is where users can read up on how the process works to buy a car and how the process works to sell a car. For both buying and selling a car, there is YouTube video to watch (works in the app) and a summary of the video in text so users can get the information multiple ways.

![AboutGif](readmegifs/aboutPage.gif)

## Still to come

What still needs to happen is:
1. Details page for each auction, showing more car and seller information
2. Details page for each auction, ability to bid
3. Details page for each auction, ability to comment
4. Ability to create user account
5. User authentication
