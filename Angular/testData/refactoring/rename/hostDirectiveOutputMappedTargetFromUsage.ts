import {Directive, Component, Output} from '@angular/core';

@Directive({
 selector: '[app-dir]',
 standalone: true,
})
export class TestDirective {
  @Output("aliased")
  output: string
}

@Component({
 selector: 'app-test',
 hostDirectives: [
   {
     directive: TestDirective,
     outputs: ["aliased: aliasedTwice"]
   }
 ],
 template: `
    <app-test (ali<caret>asedTwice)="'foo'"></app-test>
    <div app-dir (aliased)="'foo'"></div>
  `
})
export class TestComponent {

}
