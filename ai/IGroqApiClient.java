/*
MIT License

Copyright (c) 2024 Korn Kutan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package bountyprompt.ai;

import javax.json.JsonObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.Observable;

public interface IGroqApiClient {
    /**
     * Creates a chat completion asynchronously and returns a Single that emits the result.
     * @param request The JSON object containing the request parameters.
     * @return A Single that emits the resulting JSON object.
     */
    Single<JsonObject> createChatCompletionAsync(JsonObject request);

    /**
     * Creates a chat completion stream and returns an Observable that emits each JSON object
     * as they are received from the server.
     * @param request The JSON object containing the request parameters.
     * @return An Observable that emits JSON objects as they are streamed from the server.
     */
    Observable<JsonObject> createChatCompletionStreamAsync(JsonObject request);
}