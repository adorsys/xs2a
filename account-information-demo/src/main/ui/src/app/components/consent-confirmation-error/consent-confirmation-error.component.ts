import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';

@Component({
  selector: 'app-consent-confirmation-error',
  templateUrl: './consent-confirmation-error.component.html',
  styleUrls: ['./consent-confirmation-error.component.scss']
})
export class ConsentConfirmationErrorComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router){ }

  ngOnInit() {
  }

}
