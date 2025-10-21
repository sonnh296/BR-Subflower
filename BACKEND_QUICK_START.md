# AI Fashion Advisor - Quick Start Guide (Backend Only)

## What Was Implemented

I've successfully implemented an AI-powered fashion recommendation system for your Spring Boot backend. Here's what was added:

### Files Created:

**1. Configuration:**
- `OpenAIConfig.java` - Configures OpenAI service with API key from .env

**2. DTOs:**
- `FashionAdvisorRequest.java` - Request model with measurements (bust, waist, hip, skin color, etc.)
- `FashionAdvisorResponse.java` - Response model with AI recommendations

**3. Service Layer:**
- `FashionAdvisorService.java` - Service interface
- `FashionAdvisorServiceImpl.java` - Core logic with OpenAI integration and IP geolocation

**4. Controller:**
- `FashionAdvisorController.java` - REST API endpoints at `/api/fashion-advisor`

**5. Dependencies:**
- Added OpenAI Java SDK to `pom.xml` (version 0.18.2)

**6. Environment Variables:**
- Updated `.env` file with `OPENAI_API_KEY` and `OPENAI_MODEL` placeholders

## Quick Start

### Step 1: Add Your OpenAI API Key
Edit `.env` file and replace the placeholder:
```
OPENAI_API_KEY=sk-your-actual-key-here
OPENAI_MODEL=gpt-4o-mini
```

Get your API key: https://platform.openai.com/api-keys

### Step 2: Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Step 3: Test with Postman or cURL

**Endpoint 1: Get Fashion Recommendation**
```
POST http://localhost:8080/api/fashion-advisor/recommend
Content-Type: application/json

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

**Endpoint 2: Test Location Detection**
```
GET http://localhost:8080/api/fashion-advisor/location
```

## Expected Response Format

```json
{
  "code": 1000,
  "message": "Fashion recommendation generated successfully",
  "result": {
    "recommendation": "Based on your measurements, you have an hourglass body shape...",
    "userCountry": "United States",
    "userLocation": "New York, NY, United States",
    "suggestedColors": ["Navy", "Coral", "White", "Olive"],
    "suggestedStyles": ["A-line dresses", "High-waisted jeans", "Wrap tops"],
    "bodyType": "Hourglass",
    "additionalTips": "For your hourglass figure, emphasize your waist..."
  }
}
```

## Integration with Vue.js Frontend

Your Vue.js app just needs to make HTTP requests to these endpoints:

```javascript
// Example using axios
axios.post('http://localhost:8080/api/fashion-advisor/recommend', {
  skinColor: "Medium",
  bust: "86cm",
  waist: "66cm",
  hip: "91cm",
  height: "165cm",
  style: "Casual"
})
.then(response => {
  console.log(response.data.result);
  // Display the recommendations in your UI
})
```

## Key Features

✅ **Automatic Location Detection** - Uses IP address to detect user's country/city  
✅ **Smart Body Type Analysis** - AI determines body shape from measurements  
✅ **Color Recommendations** - Suggests colors based on skin tone  
✅ **Climate-Aware** - Considers local weather and fashion trends  
✅ **CORS Enabled** - Ready for Vue.js frontend integration  

## System Prompt Overview

The AI is instructed to act as a professional fashion stylist who considers:
- Body measurements and proportions
- Skin tone and complementary colors
- Regional climate and cultural fashion trends
- Current fashion trends and timeless principles

You can customize this prompt in `FashionAdvisorServiceImpl.java` → `buildSystemPrompt()` method.

## Cost Considerations

- Using `gpt-4o-mini`: ~$0.001-0.003 per recommendation
- Average tokens per request: 500-1000
- Monitor usage at: https://platform.openai.com/usage

## Troubleshooting

**"OPENAI_API_KEY must be set" error:**
- Make sure `.env` file has your actual API key
- Restart the Spring Boot application

**"Unknown Location" in response:**
- Normal for localhost testing
- Will work correctly when deployed to a server

**Slow response times:**
- OpenAI API typically responds in 2-5 seconds
- Consider adding loading indicators in your frontend

## Next Steps for Frontend Integration

You can now build your Vue.js UI to:
1. Show a form with the required fields (skinColor, bust, waist, hip, etc.)
2. Call the POST endpoint with the form data
3. Display the AI recommendations in a nice chat or card interface
4. Show the color suggestions as colored chips
5. Display the style suggestions as tags

The backend is complete and ready to serve your Vue.js frontend!

