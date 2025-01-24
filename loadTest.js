import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.2/index.js";

export let unableToConnectCounter = new Counter("unable_to_connect");
export let cacheUsedCounter = new Counter("cache_used");
export let circuitBreakerCounter = new Counter("circuit_breaker_open");
export let bonusNullCounter = new Counter("bonus_response_null");
export let responseTime5xx = new Trend("response_time5xx", true);
export let responseTime2xx = new Trend("response_time2xx", true);

export const options = {
  stages: [
    { duration: "1s", target: 80 },
    { duration: "1s", target: 80 },
    { duration: "1s", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<3000"],
  },
};

let totalRequests = new Counter("total_requests");

export default function () {
  const url = `http://localhost:8080/buy?product=1&user=1&ft=true`;

  const res = http.post(url);

  totalRequests.add(1);

  if (res.body.includes("Unable to connect to product service")) {
    unableToConnectCounter.add(1);
  }

  if (res.body.includes("cached")) {
    cacheUsedCounter.add(1);
  }

  if (res.body.includes("fallbackID")) {
    circuitBreakerCounter.add(1);
  }

  if (res.body.includes("Bonus Response: null")) {
    bonusNullCounter.add(1);
  }

  if (res.status >= 200 && res.status < 300) {
    responseTime2xx.add(res.timings.duration);
  }

  if (res.status >= 500) {
    responseTime5xx.add(res.timings.duration);
  }

  check(res, {
    "status is 200": (r) => r.status === 200,
  });

  sleep(1);
}

export function handleSummary(data) {
  const total = data.metrics.total_requests.values.count;
  const unableToConnect = data.metrics.unable_to_connect.values.count || 0;
  const cacheUsed = data.metrics.cache_used.values.count || 0;
  const circuitBreaker = data.metrics.circuit_breaker_open.values.count || 0;
  const bonusNull = data.metrics.bonus_response_null.values.count || 0;

  const summary = {
    "Metrics Summary": {
      "Total Requests": total,
      "Unable to get product (% of total)": ((unableToConnect / total) * 100).toFixed(2) + "%",
      "Exchange Cache Used (% of total)": ((cacheUsed / total) * 100).toFixed(2) + "%",
      "Circuit Breaker Open (% of total)": ((circuitBreaker / total) * 100).toFixed(2) + "%",
      "Bonus Response Null (% of total)": ((bonusNull / total) * 100).toFixed(2) + "%",
    },
  };

  console.log(JSON.stringify(summary, null, 2));

  return {
    stdout: textSummary(data, { indent: "→", enableColors: true }),
    "summary.json": JSON.stringify(summary, null, 2),
  };
}
