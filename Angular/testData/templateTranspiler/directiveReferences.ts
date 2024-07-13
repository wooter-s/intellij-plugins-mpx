/* tslint:disable */
import { Component } from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatDatepicker, MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from "@angular/material/input";

export interface Moment extends Object {

  month(): number;

  year(): number;
}

// noinspection JSUnusedLocalSymbols
function moment(_input?: string, _strict?: boolean): Moment {
  return {
    month(): number {
      return 0;
    }, year(): number {
      return 0;
    }
  }
}

/** @title Datepicker emulating a Year and month picker */
@Component({
  standalone: true,
  selector: 'datepicker-views-selection-example',
  template: `
    <mat-form-field>
      <input
        matInput
        [matDatepicker]="dp"
        placeholder="Month and Year"
        [formControl]="date"
        [max]="my"
      />
      <mat-datepicker-toggle matSuffix [for]="dp"></mat-datepicker-toggle>
      <mat-datepicker
        #dp
        startView="multi-year"
        (monthSelected)="setMonthAndYear($event, dp)"
        (yearSelected)="acceptBoolean(dp)"
        panelClass="example-month-picker"
      >
      </mat-datepicker>
    </mat-form-field>
    <mat-datepicker
      #dp2
      [startAt]="12"
      startView="multi-year"
      (monthSelected)="setMonthAndYear($event, dp2)"
      (yearSelected)="acceptBoolean(dp2)"
      panelClass="example-month-picker"
    />
  `,
  imports: [
    MatDatepickerModule,
    MatInputModule,
    ReactiveFormsModule,
  ]
})
export class DatepickerViewsSelectionExample {
  validFrom = '2022-06-17T09:08:15.382+00:00';
  date = new FormControl(moment(this.validFrom));
  my = moment();

  setMonthAndYear(
    _normalizedMonthAndYear: Moment,
    datepicker: MatDatepicker<Moment>
  ) {
    const ctrlValue = this.date.value!;
    this.date.setValue(ctrlValue);
    datepicker.close();
  }

  acceptBoolean(_arg: boolean){
  }
}

