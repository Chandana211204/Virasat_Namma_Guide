# Virasat-Namma Guide
### Smart Heritage Tourism Android Application

## Overview

->Virasat-Namma Guide is an Android-based smart tourism application designed to promote the rich cultural and historical 
heritage of Karnataka through an interactive digital experience. 
->The application helps tourists discover heritage locations, access historical information, unlock hidden facts using 
QR technology, navigate using Google Maps, and experience AI-style storytelling narration.
->The project combines Android development concepts such as APIs, Room Database, QR scanning, TextToSpeech, 
and location-based filtering to create a modern heritage tourism platform.

## Features
### Heritage Site Discovery
* Explore Karnataka heritage places
* Heritage cards with images and descriptions
* Radius-based nearby filtering
* Distance calculation using Haversine Formula

### Heritage Detail Page
* Historical information
* Architecture details
* Legends and cultural significance
* Site images and descriptions

### QR Hidden Fact System

* QR generation for hidden facts
* Camera QR scanning
* Gallery QR scanning
* Unlock hidden heritage information

### Travel Passport

* Check-in visited places
* Room Database integration
* Persistent visited stamps system

### Maps Navigation

* Open Google Maps route
* Current location to heritage site navigation

### Live Weather Integration

* Real-time weather information
* Temperature, wind speed, and conditions
* Open-Meteo API integration

### GenAI Story Mode

* AI-style heritage storytelling
* TextToSpeech narration
* Immersive heritage experience

### Temple-Inspired UI

* Karnataka heritage color palette
* Maroon, sandstone, and gold theme
* Elegant heritage-inspired design

## Tech Stack

| Technology            | Usage               |
| --------------------- | ------------------- |
| Kotlin                | Android Development |
| Android Studio        | IDE                 |
| Room Database         | Local Data Storage  |
| Google ML Kit / ZXing | QR Scanning         |
| Open-Meteo API        | Weather Information |
| Google Maps Intent    | Navigation          |
| TextToSpeech          | Story Narration     |
| RecyclerView          | Heritage Listing    |
| Material Design       | UI Components       |

## Project Structure
com.virasatnammaguide
│
├── activities
├── adapters
├── model
├── repository
├── database
├── viewmodel
├── utils
├── api
└── res

## Main Modules

* SplashActivity
* WelcomeActivity
* MainActivity
* DetailActivity
* ScannerActivity
* HiddenFactActivity
* PassportActivity
* StoryActivity

## Installation Steps

### Clone Repository

```bash
git clone https://github.com/chandana211204/Virasat_Namma_Guide.git
```

### Open in Android Studio
* Open Android Studio
* Select “Open Project”
* Choose the cloned folder

### Sync Gradle
Allow Gradle dependencies to download completely.

### Run Application
Connect Android device or emulator and run the project.

## Permissions Used

```xml
INTERNET
CAMERA
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
```

## Current Features Status

| Feature                 | Status             |
| ----------------------- | ------------------ |
| QR Hidden Fact System   | Completed          |
| Room Database Check-in  | Completed          |
| Google Maps Navigation  | Completed          |
| Weather API             | Completed          |
| Story Mode              | Completed          |
| TextToSpeech            | Partially Improved |
| AR Heritage Exploration | In Progress        |
| Offline Map Support     | In Progress        |
| Multi-language Support  | Planned            |


## Future Enhancements

* Offline Map Support
* AR Heritage Exploration
* Kannada + English Localization
* Tourist Quiz System
* Achievement Badges
* Audio Guide Improvements
* Firebase Integration
* Cloud Backup Support

## Learning Outcomes

This project helped in learning:

* Android application development using Kotlin
* Room Database integration
* API handling and networking
* QR code generation and scanning
* Google Maps integration
* TextToSpeech implementation
* RecyclerView and custom UI design
* Location and distance calculation concepts

## Author

Chandana
Computer Science Engineering Student
Android App Development Internship Project

## Project Goal
To create an interactive smart tourism platform that digitally preserves and presents Karnataka’s 
cultural heritage using modern Android technologies.
