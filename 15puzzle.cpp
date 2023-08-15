#include <FL/Fl.H> // needed in every fltk program
#include <FL/Fl_Window.H> // needed to use the Fl_Window class
#include <FL/Fl_Button.H> // needed to use the Fl_Box class
#include <FL/Fl_Radio_Button.H> // needed to use the Fl_Box class
#include <FL/Fl_Light_Button.H> // needed to use the Fl_Box class

char *int2charstar (int v) {
 char *s = new char[sizeof(int)];
 sprintf(s,"%d",v);
 return s;
};


class MyButton : public Fl_Button {
	int count;
	public:
		MyButton (int x, int y, int w, int h, const char *l); 
		int handle(int e); // e is incoming mouse event of different kinds
 };

MyButton :: MyButton (int x,int y, int w, int h, const char *l) : Fl_Button (x,y,w,h,l) {
	count =0;
}
int MyButton :: handle (int e) {
	
};

//int main(int argc, char *argv[]) {
//
  //       char* (int k, int m){
    //     char s[2];
    //     int number= 4*k+m+1;
    //     int first_digit=number/10;
    //     int second_digit=number%10;
         
     
                 
        
    	 Fl_Window *window;

window = new Fl_Window (600,600,"DEMO"); // outer window

        Fl_Button *Game = new MyButton[4][4];
	window->color(FL_WHITE);
	button1->down_color(FL_RED);
	for(int i=0; i<4; i++){
	        for(int j=0; j<4; j++){
	                Game[i][j]((j+1)*30, (i+1)*30, 25,25,int2charstar(4*i+j+1) );
	                j++;
                }                 
	}

	//button1->label("10");


    	window->end();
    	window->show();
    	return(Fl::run());  // the process waits from here on for events
}


