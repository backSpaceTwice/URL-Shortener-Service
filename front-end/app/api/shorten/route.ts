import { NextRequest, NextResponse } from "next/server";

type BackendResponse = {
  code?: string;
  message?: string;
};

const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const response = await fetch(`${backendUrl}/api/urls`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url: body.url }),
      cache: "no-store",
    });
    const responseBody = (await response.json()) as BackendResponse;

    if (!response.ok || !responseBody.code) {
      return NextResponse.json(
        { message: responseBody.message || "Unable to shorten this URL." },
        { status: response.status }
      );
    }

    return NextResponse.json({
      shortUrl: `${backendUrl}/r/${responseBody.code}`,
    });
  } catch {
    return NextResponse.json(
      { message: "The URL service is unavailable." },
      { status: 502 }
    );
  }
}
