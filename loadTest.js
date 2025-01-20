import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend, Rate, Gauge } from 'k6/metrics';

export const options = {
  stages: [
    { duration: "3s", target: 100 },
    { duration: "3s", target: 100 },
    { duration: "3s", target: 0 },
  ],
};

const responseTime5xx = new Trend("response_time5xx", true);
const responseTime2xx = new Trend("response_time2xx", true);

export default function () {
  const randomProduct = Math.floor(Math.random() * 1000) + 1;
  const randomUser = Math.floor(Math.random() * 1000) + 1;
  const url = `http://localhost:8080/buy?product=${randomProduct}&user=${randomUser}&ft=true`;

  const res = http.post(url);

  if (res.status >= 200 && res.status < 300) {
    responseTime2xx.add(res.timings.duration);
  }

  if (res.status >= 500) {
    responseTime5xx.add(res.timings.duration);
  }

  check(res, {
    "status é 2xx": (r) => r.status >= 200 && r.status < 300,
    "status é 5xx (erro no servidor)": (r) => r.status >= 500,
    "Produto não encontrado": (r) => r.body === "Unable to connect to product service",
    "Taxa de cambio veio do cache": (r) => r.body.includes("cached"),
    "Exchange caiu os dois pods": (r) => r.body === "No exchange service response",
    "Não foi possível realizar a venda": (r) => r.body === "No sell service response",
    "Não conseguiu salvar o bonus durante o request": (r) =>
      r.body.includes("Bonus Response: null"),
  });

  check(res, {
    "tempo < 200ms": (r) => r.timings.duration < 200,
    "tempo < 1s": (r) => r.timings.duration >= 200 && r.timings.duration < 1000,
    "tempo < 2s": (r) => r.timings.duration >= 1000 && r.timings.duration < 2000,
    "tempo < 3s": (r) => r.timings.duration >= 2000 && r.timings.duration < 3000,
    "tempo > 3s e tempo < 5s": (r) => r.timings.duration >= 3000 && r.timings.duration < 5000,
    "request timeout (tempo > 30s)": (r) => r.timings.duration > 5000 && r.timings.duration > 30000,
  });

  sleep(1);
}
