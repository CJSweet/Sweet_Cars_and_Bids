# A Sweet Cars and Bids App (as of 9/17/2020)
If [Cars & Bids](https://carsandbids.com/) were to have an Android app, this would be it.


Doug Demuro is a car reviewer on YouTube and he just launched a company called Cars & Bids, a car auction website for cool enthusiast cars, as well as quirky ones. I have been watching Doug for at least six years now and when he annouced the new website it was about the same time I decided to get serious about pursuing an Android development career. Currently, there is no Android app for it on the Google Play store so I thought what a perfect project for me! This app allows me to work with a long time hobby of mine: learning about cars and the auto industry, and do something I love: making apps! So, this is the app that maybe someday Cars & Bids will use for their company. Fingers crossed. If not however, then I will have gained great experience working with Android.

This app uses the MVVM Android architecture and is coded all in Kotlin. It uses Firebase Storage to hold the images of the cars, and Firebase Firestore to hold all other car data. All UI components were heavily influenced by the Cars & Bids website ([https://carsandbids.com/](https://carsandbids.com/)). Below, I have sectioned off different parts of this README to correlate with the app so you can get a good tour of the app without having to download it yourself. If I get to the point where I implement all the basic functions I want, I plan to make a YouTube video detailing everything that the app has to offer. 

## The Auctions Fragment

This is the page users will first see when they open the app. This is where all car auctions are listed. Once the user selects a listing, the app will navigate to a more detailed page to see more information about the car and seller, and that will be where the user can bid on the selected car. 

Each listing on the auction page shows a picture of the outside of the car, the time until the auction closes, the current bid, a sentence summarizing some cool features or modifications of the car, and it's location. (As shown in the GIF below)

![AuctionsGif](readmegifs/listingsPageGif.gif)

## The Detailed Listing Fragment

After a use clicks a listing on the auctions fragment, they will be brought to the detailed listing fragment. This is where users will be able to see all the information about the car including images (though currently it's only the first image seen), the car details, highlights, any modifications, flaws, and videos of the car. Also there is the "Casey's Take" section where I give my personal take on the car and highlight some things I like about the car. This is a spoof of "Doug's Take" on the website, and my take is the same for each vehicle since the text is hard coded currently. Eventually, this will also be where user can place their bids for the car and submit and read comments in the comments section that will be at the bottom of the fragment. Another future feature will be when the auction timer is less than a minute, the black box that shows the timer, high bid, etc. will turn a red-orange color and turn back to black in a reverse progress bar fashion (full red to black from right to left) as the timer reaches zero. 

![DetailGif](readmegifs/detailPageGif.gif)

## The Sell A Car Fragment

If the user would like to submit their vehicle to be reviewed for acceptance to be sold on Cars & Bids, this is the form they fill out. It is all basic and necessary info that will be needed if the car is to be auctioned on Cars & Bids. Users can select photos from their phone's gallery to be uploaded, or take a photo on their camera through the app. The photo array submitted can be edited whenever the user likes before submission. All needed fields have validation checks applied to them and will show errors to the user where more and or proper info is still needed. 

The GIF below shows most of the features of this fragment, save for images and photo capturing with the camera. When in the "Photos" section of the form, if user selects "Select Photo" or "Take Photo" first permissions are asked of the user if the app can have access to read and or write to files and access the camera. Once the user selects a photo or takes a picture, the image is shown in the white box. Beneath each photo is a delete button that when clicked will delete said image. If there are any photos that have been selected a third button appears between the "Select Photo" and "Take Photo" button labeled "Clear All" and this will delete all photos from the box. 

As you can see in the GIF, there are validation checks and users receive a Toast message saying "Invalid Entries" if submit button is clicked with improper information. The errors are either shown by red text below the needed view or the whole view turns red with red text below. 

This fragment by far is the one I have had to spend the most amount of time on to get anywhere close to where I want it to be. There is still a lot of features to implement and changes I want to make, but for now it is in a good enough spot.

![SellCarGif](readmegifs/submitCarPageGif.gif)

## The About Fragment

Here is where most questions can be answered about what Cars & Bids is and how it functions. The top half of the page is the "what" of the about page, meaning it is where users can read basic information on what Cars & Bids is. The second half of the page is the "how," meaning this is where users can read up on how the process works to buy a car and how the process works to sell a car. For both buying and selling a car, there is YouTube video to watch (which works in the app) and a summary of the video in text so users can get the information multiple ways.

![AboutGif](readmegifs/aboutPageGif.gif)

## Still to come

Some of the features I know for sure still need implementing:
1. Ability to bid in details fragment
2. Ability to comment in details fragment
3. Ability to create user account
4. User authentication
5. Change Icon
