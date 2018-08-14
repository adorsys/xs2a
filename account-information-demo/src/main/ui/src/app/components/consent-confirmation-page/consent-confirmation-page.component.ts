import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';


@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html',
  styleUrls: ['./consent-confirmation-page.component.scss']
})
export class ConsentConfirmationPageComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit() {
  }

}
