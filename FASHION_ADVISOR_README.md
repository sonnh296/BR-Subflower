# AI Fashion Advisor Feature - Backend API

## Overview
This backend API provides personalized fashion recommendations using OpenAI's ChatGPT. Users can send their body measurements, skin tone, and style preferences, and receive AI-powered clothing and dress recommendations tailored to their body type, location, and climate.

## Features
- 🎨 Personalized fashion recommendations based on body measurements
- 🌍 Location-based climate and regional fashion trend considerations using IP geolocation
- 💡 AI-powered color theory and body type analysis
- 📍 Automatic user location detection (country, city) from request IP
- 🎯 Style and occasion-specific suggestions
- 🔒 RESTful API with CORS support for Vue.js frontend integration

## Backend Components

### 1. Configuration
- **OpenAIConfig.java** - Configures OpenAI service with API key from environment variables

### 2. DTOs
- **FashionAdvisorRequest.java** - Request model with user measurements and preferences
- **FashionAdvisorResponse.java** - Response model with recommendations and styling tips

### 3. Service Layer
- **FashionAdvisorService.java** - Service interface
- **FashionAdvisorServiceImpl.java** - Implementation with OpenAI integration and IP geolocation

### 4. Controller
- **FashionAdvisorController.java** - REST API endpoints

## API Endpoints

### POST /api/fashion-advisor/recommend
Get personalized fashion recommendations

**Request Body:**
```json
{
  "skinColor": "Medium",
  "bust": "86cm",
  "waist": "66cm",
  "hip": "91cm",
  "height": "165cm",
  "weight": "55kg",
  "style": "Casual",
  "occasion": "Daily wear",
  "additionalInfo": "I prefer comfortable clothes"
}
```

**Response:**
```json
{
  "code": 1000,
  "message": "Fashion recommendation generated successfully",
  "result": {
    "recommendation": "Detailed AI-generated recommendation...",
    "userCountry": "United States",
    "userLocation": "New York, NY, United States",
    "suggestedColors": ["Navy", "Coral", "White", "Olive"],
    "suggestedStyles": ["A-line dresses", "High-waisted jeans", "Wrap tops"],
    "bodyType": "Hourglass",
    "additionalTips": "Additional styling tips..."
  }
}
```

### GET /api/fashion-advisor/location
Get user's detected location

**Response:**
```json
{
  "code": 1000,
  "message": "Location detected successfully",
  "result": "New York, NY, United States"
}
```

## Setup Instructions

### 1. Add OpenAI API Key
Add your OpenAI API key to the `.env` file (already done):
```
OPENAI_API_KEY=your_actual_openai_api_key_here
OPENAI_MODEL=gpt-4o-mini
```

**Get your API key from:** https://platform.openai.com/api-keys

### 2. Install Dependencies
The OpenAI Java SDK dependency has been added to `pom.xml`. Run Maven install:
```bash
mvn clean install
```

### 3. Run the Application
Start the Spring Boot application:
```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/api/fashion-advisor`

### 4. Test the API
You can test the endpoints using tools like Postman, cURL, or from your Vue.js frontend.

## Testing Examples

### Using cURL (Windows CMD)
```bash
curl -X POST http://localhost:8080/api/fashion-advisor/recommend ^
  -H "Content-Type: application/json" ^
  -d "{\"skinColor\":\"Medium\",\"bust\":\"86cm\",\"waist\":\"66cm\",\"hip\":\"91cm\",\"height\":\"165cm\",\"style\":\"Casual\",\"occasion\":\"Daily wear\"}"
```

### Using cURL (PowerShell)
```powershell
$body = @{
    skinColor = "Medium"
    bust = "86cm"
    waist = "66cm"
    hip = "91cm"
    height = "165cm"
    style = "Casual"
    occasion = "Daily wear"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/fashion-advisor/recommend" -Method Post -Body $body -ContentType "application/json"
```

### Test Location Detection
```bash
curl http://localhost:8080/api/fashion-advisor/location
```

## How It Works

1. **Request Received**: Vue.js frontend (or any client) sends user's measurements and preferences
2. **Location Detection**: Backend automatically detects user's location using their IP address via ipapi.co
3. **AI Processing**: 
   - A specialized system prompt instructs ChatGPT to act as a professional fashion stylist
   - User information is formatted and sent to OpenAI
   - AI analyzes body type, skin tone, and location to generate recommendations
4. **Response Parsing**: Backend parses the AI response and structures it into a user-friendly JSON format
5. **Return**: Structured JSON response is sent back to the client for display

## System Prompt
The AI is provided with a comprehensive system prompt that instructs it to:
- Analyze body measurements and determine body type
- Consider skin tone for color recommendations
- Factor in regional climate and fashion trends
- Provide specific, actionable advice
- Format response in structured JSON

## IP Geolocation
The service uses ipapi.co (free tier) to detect user location from their IP address. This helps provide:
- Climate-appropriate recommendations
- Regional fashion trend awareness
- Cultural considerations

**Supported Headers for IP Detection:**
- X-Forwarded-For
- X-Real-IP
- Proxy-Client-IP
- And other standard proxy headers

## Customization

### Change AI Model
Update `OPENAI_MODEL` in `.env`:
- `gpt-4o-mini` (faster, cheaper, recommended)
- `gpt-4` (more accurate, expensive)
- `gpt-3.5-turbo` (balanced)

### Adjust AI Behavior
Modify the system prompt in `FashionAdvisorServiceImpl.java` method `buildSystemPrompt()` to change how the AI responds. You can:
- Add more specific fashion expertise
- Include brand recommendations
- Adjust the tone (more casual/formal)
- Add cultural considerations

### Add More Form Fields
1. Add fields to `FashionAdvisorRequest.java`
2. Update the `buildUserMessage()` method in `FashionAdvisorServiceImpl.java` to include new fields
3. Your Vue.js frontend will need to send these new fields

## Security Considerations

1. **API Key Protection**: Never commit your OpenAI API key to version control
2. **Rate Limiting**: Consider implementing rate limiting to prevent API abuse
3. **Input Validation**: All user inputs are validated on the backend
4. **CORS**: Update CORS configuration in production to allow only your frontend domain

## Cost Considerations

- OpenAI API calls are charged per token
- Average request cost with gpt-4o-mini: ~$0.001-0.003 per recommendation
- Consider implementing caching for similar requests
- Monitor usage via OpenAI dashboard

## Troubleshooting

### "OPENAI_API_KEY must be set" error
- Make sure your `.env` file has the correct API key
- Restart the Spring Boot application after adding the key

### Location detection returns "Unknown Location"
- This is normal for localhost development
- The service defaults to a test IP for location detection
- Will work correctly when deployed to a server

### AI response parsing errors
- Check OpenAI API status: https://status.openai.com
- Verify your API key has sufficient credits
- Check application logs for detailed error messages

## Future Enhancements

- [ ] Add conversation history for follow-up questions
- [ ] Image upload for color matching
- [ ] Product recommendations from your store
- [ ] Save favorite recommendations
- [ ] Share recommendations via social media
- [ ] Multi-language support

## License
This feature is part of the Sunflower e-commerce application.

