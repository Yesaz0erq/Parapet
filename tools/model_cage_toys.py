"""Make the editable cage toys. Run paint_cage_toys.py first and export_aviary_furniture.py afterwards."""
from model_aviary_supplies import Project


class Toy(Project):
    def piece(self, name, x,y,z, X,Y,Z, material):
        return self.box("frame",name,(x,y,z),(X,Y,Z),material)

    def roof_clip(self):
        # Actual roof bars run through this opening in Z. Centre of the bore: (8,15,8).
        self.piece("clip_bottom",6.05,13.22,7.38,9.95,13.8,8.62,"brass")
        self.piece("clip_left",6.05,13.8,7.38,6.8,16.2,8.62,"brass")
        self.piece("clip_right",9.2,13.8,7.38,9.95,16.2,8.62,"brass")
        self.piece("clip_latch",6.05,16.2,7.38,9.95,16.75,8.62,"brass")

    def side_clip(self):
        # A cage's vertical side bar runs through (8,12.4,14), between the two plates.
        self.piece("clamp_face",6.10,11.4,12.04,9.9,13.4,12.78,"brass")
        self.piece("clamp_left",6.10,11.4,12.78,6.75,13.4,15.22,"iron")
        self.piece("clamp_right",9.25,11.4,12.78,9.9,13.4,15.22,"iron")
        self.piece("clamp_retainer",6.10,11.4,15.22,9.9,13.4,15.82,"brass")
        self.piece("clamping_screw",7.42,11.88,12.78,8.58,12.92,14.06,"iron")

    def ring(self,label,x,y,z,rx,ry,material):
        # Horizontal courses meet edge to edge, with no overlapping front faces.
        w=.87; corner=1.1
        self.piece(label+"_top",x-rx+corner,y+ry-w,z,x+rx-corner,y+ry,z+1.03,material)
        self.piece(label+"_bottom",x-rx+corner,y-ry,z,x+rx-corner,y-ry+w,z+1.03,material)
        for side,s in (("l",-1),("r",1)):
            a=x-rx if s<0 else x+rx-w
            self.piece(label+"_"+side,a,y-ry+1.59,z,a+w,y+ry-1.59,z+1.03,material)
            a=x-rx+.39 if s<0 else x+rx-1.53
            for suffix,b,B in (("lower",y-ry+w,y-ry+1.59),("upper",y+ry-1.59,y+ry-w)):
                self.piece(label+"_"+side+suffix,a,b,z,a+1.14,B,z+1.03,material)

    def carved_bead(self,label,x,y,z,w,h,d,mat):
        self.piece(label+"_centre",x-w/2,y,z-d/2+.50,x+w/2,y+h,z+d/2-.5,mat)
        for side,front in (("front",z-d/2),("rear",z+d/2-.50)):
            self.piece(label+"_"+side,x-w/2+.6,y,front,x+w/2-.6,y+h,front+.50,mat)


def rubbing_perch():
    m=Toy("beak_rubbing_perch");m.side_clip();p=m.piece
    p("seat",6.65,10.7,10.6,9.35,13.1,12.04,"walnut")
    p("bark_shoulder",6.35,10.36,8.1,9.65,13.12,10.6,"perch_bark")
    p("shaft",6.55,10.16,3.90,9.4,12.82,8.1,"worn_perch_wood")
    p("natural_bark_strip",6.42,10.64,4.65,6.55,12.12,8.1,"perch_bark")
    p("worn_face",7.0,12.82,4.3,8.98,13.02,7.7,"worn_perch_wood")
    p("tapered_tip",6.7,10.43,2.25,9.21,12.61,3.90,"perch_bark")
    p("pale_endgrain",7.03,10.70,2.12,8.88,12.31,2.25,"worn_perch_wood")
    p("twig_spur",9.4,10.78,5.05,10.74,12.24,6.68,"perch_bark")
    m.save()


def mineral_charm():
    m=Toy("mineral_chew_charm");m.side_clip();p=m.piece
    p("hook_arm",7.27,11.56,7.25,8.73,12.55,12.04,"brass")
    p("leather_loop",7.59,9.57,7.4,8.41,11.56,8.20,"twine")
    m.carved_bead("clay_bead",8,8.83,7.8,2.42,1.45,2.40,"toy_rosewood")
    p("stone_tie",7.55,7.38,7.42,8.45,8.83,8.2,"twine")
    m.carved_bead("shoulder",8.0,6.36,7.8,5.24,1.14,2.96,"toy_mineral")
    m.carved_bead("mineral_face",7.84,3.72,7.8,6.5,2.64,3.7,"toy_mineral")
    m.carved_bead("taper",7.67,2.70,7.75,4.62,1.02,2.85,"toy_mineral")
    p("lower_knot",7.25,1.91,7.29,8.33,2.70,8.25,"twine")
    p("frayed_tail",7.02,.87,7.34,7.62,1.91,7.98,"toy_sisal")
    m.save()


def rope_swing():
    m=Toy("rope_swing_cage");m.roof_clip();p=m.piece
    p("binding",7.4,12.12,7.44,8.6,13.22,8.56,"toy_sisal")
    p("spreader",3.65,11.07,7.36,12.35,12.12,8.64,"honey_oak")
    for i,x in enumerate((3.82,11.28)):
        p("suspension_"+str(i),x,3.65,7.52,x+.9,11.07,8.48,"toy_sisal")
        p("stopper_"+str(i),x-.16,2.62,7.32,x+1.06,3.62,8.68,"twine")
    p("swing_perch",2.62,3.48,7.18,13.38,5.0,8.82,"perch_bark")
    m.save()


def chew_blocks():
    m=Toy("chew_blocks_cage");m.side_clip();p=m.piece
    p("bracket",7.16,11.42,7.45,8.84,12.50,12.04,"walnut")
    p("string",7.67,1.18,7.64,8.33,11.42,8.28,"twine")
    for i,(y,w,mat) in enumerate(((2,4.4,"toy_rosewood"),(5.1,3.8,"toy_honeywood"),(8.0,3.4,"toy_tealwood"))):
        m.carved_bead("carved_"+str(i),8,y,8,w,1.64,3.2,mat)
        p("knot_"+str(i),7.47,y+1.64,7.44,8.53,y+2.18,8.56,"twine")
    m.save()


def double_swing():
    m=Toy("double_ring_swing");m.roof_clip();p=m.piece
    p("yoke",5.2,12.34,7.39,10.8,13.22,8.61,"toy_sisal")
    p("left_binding",5.12,10.65,6.82,6.12,12.34,7.85,"toy_sisal")
    p("right_binding",9.94,8.86,8.12,10.91,12.34,9.16,"toy_sea_rope")
    m.ring("hemp_ring",5.60,7.4,6.82,3.12,3.38,"toy_sisal")
    m.ring("green_ring",10.42,5.53,8.12,3.12,3.46,"toy_sea_rope")
    m.save()


def mobile():
    m=Toy("large_chew_mobile");m.roof_clip();p=m.piece
    p("swivel",7.46,11.74,7.48,8.54,13.22,8.52,"iron")
    m.carved_bead("hub",8,10.55,8,2.6,1.19,2.42,"toy_rosewood")
    p("arm_x",2.05,9.52,7.40,13.95,10.55,8.60,"worn_perch_wood")
    p("arm_z",7.37,8.62,3.00,8.63,9.52,13.0,"honey_oak")
    for i,(x,z,low,top,mat,w) in enumerate(((2.98,8,4.1,9.52,"toy_sea_rope",2.78),
        (12.99,8,3.06,9.52,"toy_rosewood",3.15),(8,3.6,1.38,8.62,"toy_honeywood",2.94),
        (8,12.36,4.74,8.62,"toy_tealwood",2.92))):
        p("drop_"+str(i),x-.30,low-.77,z-.30,x+.30,top,z+.30,"twine")
        m.carved_bead("block_"+str(i),x,low,z,w,1.9,2.48,mat)
        if i%2==0:
            p("bottom_bead_"+str(i),x-.62,low-.78,z-.57,x+.62,low-.12,z+.57,"toy_rosewood")
    m.save()


if __name__=="__main__":
    for create in (rubbing_perch,mineral_charm,rope_swing,chew_blocks,double_swing,mobile):
        create()
