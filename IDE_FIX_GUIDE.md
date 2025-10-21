# IntelliJ IDEA - Fix "package does not exist" Error

## The Problem
Your IDE shows: `java: package com.theokanning.openai.service does not exist`

But Maven compiles successfully! This is an IDE cache/indexing issue.

## Solutions (Try in order):

### Solution 1: Reimport Maven Project (Quickest)
1. Right-click on the `pom.xml` file in Project view
2. Select **Maven** → **Reload Project**
3. Wait for IntelliJ to finish indexing (bottom right corner)

### Solution 2: Invalidate Caches
1. Go to **File** → **Invalidate Caches...**
2. Check all boxes (Invalidate and Restart)
3. Click **Invalidate and Restart**
4. Wait for IntelliJ to reindex your project

### Solution 3: Reimport All Maven Projects
1. Open the Maven tool window (View → Tool Windows → Maven)
2. Click the refresh icon (🔄) "Reload All Maven Projects"
3. Wait for completion

### Solution 4: Manual Dependency Download
1. In Maven tool window, expand **sunflower** → **Lifecycle**
2. Double-click **clean**
3. Double-click **compile**
4. Then do Solution 1 or 2

### Solution 5: Delete IDE Files (If all else fails)
1. Close IntelliJ IDEA
2. Delete these folders from project root:
   - `.idea` folder
   - `*.iml` files
3. Reopen the project (IntelliJ will reimport everything)

## Verification
After trying any solution, check if the error is gone:
- Open `OpenAIConfig.java`
- The import `com.theokanning.openai.service.OpenAiService` should be green (not red)
- No compilation errors should appear

## Note
Maven successfully compiled your project, so the dependency IS there. This is purely an IDE synchronization issue.

