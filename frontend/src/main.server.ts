import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

export default function bootstrap() {
  return bootstrapApplication(App, {
    ...appConfig,
  });
}