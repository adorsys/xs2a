import { Component, OnInit } from '@angular/core';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-help-page',
  templateUrl: './help-page.component.html',
  styleUrls: ['./help-page.component.css']
})
export class HelpPageComponent implements OnInit {
  SWAGGER_URL = `${environment.xs2aServerUrl}/swagger-ui.html#`;

  constructor() { }

  ngOnInit() {
  }

}
