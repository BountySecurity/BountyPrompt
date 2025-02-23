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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.io.StringReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroqApiClientImpl implements IGroqApiClient {

    private final String apiKey;
    private final HttpClient client;

    public GroqApiClientImpl(String apiKey) {
        ExecutorService executor = Executors.newCachedThreadPool();
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .executor(executor)
                .build();
    }

    @Override
    public Single<JsonObject> createChatCompletionAsync(JsonObject request) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(request.toString(), StandardCharsets.UTF_8))
                .build();

        return Single.<HttpResponse<String>>create(emitter -> {
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(emitter::onSuccess)
                    .exceptionally(throwable -> {
                        emitter.onError(throwable);
                        return null;
                    });
        }).map(HttpResponse::body)
                .map(body -> {
                    try {
                        return Json.createReader(new StringReader(body)).readObject();
                    } catch (Exception e) {
                        return Json.createObjectBuilder().add("error", body).build();
                    }
                });
    }

    @Override
    public Observable<JsonObject> createChatCompletionStreamAsync(JsonObject request) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(request.toString(), StandardCharsets.UTF_8))
                .build();

        return Observable.<String>create(emitter -> {
            client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(httpResponse -> {
                try {
                    String[] lines = httpResponse.body().split("\n");
                    for (String line : lines) {
                        if (emitter.isDisposed()) {
                            break;
                        }
                        emitter.onNext(line);
                    }
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }).exceptionally(throwable -> {
                emitter.onError(throwable);
                return null;
            });
        }).filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6))
                .filter(jsonData -> !jsonData.equals("[DONE]"))
                .map(jsonData -> Json.createReader(new StringReader(jsonData)).readObject());
    }
}
