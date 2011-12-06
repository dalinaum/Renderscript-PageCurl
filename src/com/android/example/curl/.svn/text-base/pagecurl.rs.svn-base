/**
 * pagecurl.rs
 * Copyright (c) 2011 daoki2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#pragma version(1)
#pragma rs java_package_name(com.android.example)

#include "rs_graphics.rsh"

#define SEGMENT_W 5
#define SEGMENT_H 1
#define DEGREE (M_PI / 180)
#define MAX_ROTATION 180

rs_program_vertex gProgVertex;
rs_program_fragment gProgFragmentTexture;
rs_program_store gProgStoreBlendNone;
rs_program_raster gCullNone;
rs_sampler gLinearClamp;

rs_allocation gTex_00;
rs_allocation gTex_01;
rs_allocation gTex_02;
rs_allocation gTex_03;
rs_allocation gTex_04;

typedef struct __attribute__((packed, aligned(4))) Bitmaps {
    rs_allocation data;
} Bitmaps_t;
Bitmaps_t *bitmap;

static float offset_x = -256 + 256;
static float offset_y = -256;

static float base_vertices[36] = { // (SEGMENT_W + 1) * (SEGMENT_H + 1) * 3
	    0,   0,   0,   0, 512,   0,
	  198,   0,   0, 198, 512,   0,
	  314,   0,   0, 314, 512,   0,
	  396,   0,   0, 396, 512,   0,
	  460,   0,   0, 460, 512,   0,
	  512,   0,   0, 512, 512,   0
};

static float myVertices[36];

static float vertices[36] = {
	    0,   0,   0,   0, 512,  0,
	  198,   0,   0, 198, 512,  0,
	  314,   0,   0, 314, 512,  0,
	  396,   0,   0, 396, 512,  0,
	  460,   0,   0, 460, 512,  0,
	  512,   0,   0, 512, 512,  0
};

float gPx = 0;
float gPy = 0;
float gVx = 0;
float gVy = 0;

//float gDt = 0;

void init() {
}

// hard-coded magic numbers (!) :P 
static float boundary1 = 55; 
static float boundary2 = 130; 
static float boundary1Mod = 1.35;
static float boundary2Mod = 0.6;

static float vStripWidths[SEGMENT_W] = {198, 116, 82, 64, 52};

static void calcVertices(float degRot) {
	if (degRot<0) degRot=0;
	if (degRot>MAX_ROTATION)
		degRot=MAX_ROTATION;

 	float r = -degRot * DEGREE;				
	float rMod;
						
	// Calculate rotation amounts for each rectangle and store in vStripRots

	// [A] Applies to all degrees
	rMod = boundary1Mod;
			
	// [B] Applies to all degrees > boundary1
	if (degRot > boundary1) {
		float a = degRot - boundary1; // range: 0 to MAX_ROTATION - B1
		a = a / (boundary2 - boundary1) * boundary2Mod; // range: 0 to B2MOD
		rMod -= a;	// range: B1MOD to B1MOD-B2MOD
	}	

	float vStripRots[SEGMENT_W + 1];
	// Recursively multiply vStripRots elements by rMod
	for (int i = 0; i < (SEGMENT_W + 1); i++) {
		vStripRots[i] = r;
		r *= rMod;
	}

	// [C] Applies to degrees > boundary2. 
	// 	   Grow vStripRots proportionally to MAX_ROT. (Note the 'additive' nature of these 3 steps).
	if (degRot >= boundary2) {
		for (int j = 0; j < (SEGMENT_W + 1); j++) {
			float diff = MAX_ROTATION*DEGREE - fabs(vStripRots[j]);
			float rotMult = degRot - boundary2; // range: 0 to 30
			rotMult = rotMult / (MAX_ROTATION - boundary2); // range: 0 to 1
			vStripRots[j] -= diff * rotMult; // range: __ to MAX_ROTATION
		}
	}
			
	// [2] Create myVertices[]
	for (int k = 0; k < (SEGMENT_W + 1) * (SEGMENT_H + 1) * 3; k = k + 3) {
		int idx = floor((float)((k/3) / (SEGMENT_H + 1))) - 1;
		myVertices[k] = (idx >= 0) ? vStripWidths[idx] : base_vertices[k];
		myVertices[k + 1] = base_vertices[k + 1];
		myVertices[k + 2] = base_vertices[k + 2];
	}

	// [3] Apply rotation to myVerts[]
	for (int l = (SEGMENT_H + 1) * 3; l < (SEGMENT_W + 1) * (SEGMENT_H + 1) * 3; l = l + 3) {
		int idx2 = floor((float)((l/3) / (SEGMENT_H + 1))) - 1;
		myVertices[l]     = cos(vStripRots[idx2]) * myVertices[l]     - sin(vStripRots[idx2]) * myVertices[l + 2];
		myVertices[l + 1] = myVertices[l + 1];
		myVertices[l + 2] = cos(vStripRots[idx2]) * myVertices[l + 2] + sin(vStripRots[idx2]) * myVertices[l];
	}

	// [4] 'connect' the rectangles
	for (int m = (SEGMENT_H + 1) * 2 * 3; m < (SEGMENT_W + 1) * (SEGMENT_H + 1) * 3; m = m + 3) { // (first 2 edges are fine)
		//myVerts[m].x += myVerts[ m - (segmentsH+1) ].x;
		//myVerts[m].z += myVerts[ m - (segmentsH+1) ].z; // (y stays constant)
		myVertices[m]     += myVertices[m - (SEGMENT_H + 1) * 3];
		myVertices[m + 2] += myVertices[m - (SEGMENT_H + 1) * 3 + 2];
	}
		
	int i = 0;
	for (int x = 0; x < (SEGMENT_W + 1); x++) {
		vertices[x * 3    ] = myVertices[i++];
		vertices[x * 3 + 1] = myVertices[i++];
		vertices[x * 3 + 2] = myVertices[i++];
	
		int pos = (SEGMENT_W + 1 + x) * 3;
		vertices[pos]     = myVertices[i++];
		vertices[pos + 1] = myVertices[i++];
		vertices[pos + 2] = myVertices[i++];
	}
}

static void displayPageCurl() {    
    // Default vertex shader
    rsgBindProgramVertex(gProgVertex);

    // Setup the projection matrix
    rs_matrix4x4 proj;    
    float aspect = (float)rsgGetWidth() / (float)rsgGetHeight();
    rsMatrixLoadPerspective(&proj, 30.0f, aspect, 0.1f, 1500.0f);    
    rsgProgramVertexLoadProjectionMatrix(&proj);
    
    // Fragment shader with texture
    rsgBindProgramStore(gProgStoreBlendNone);
    rsgBindProgramFragment(gProgFragmentTexture);
	rsgBindProgramRaster(gCullNone);
	rsgBindSampler(gProgFragmentTexture, 0, gLinearClamp);
    
    rs_matrix4x4 matrix;
    rsMatrixLoadTranslate(&matrix, 0.0f, 0.0f, -1480.0f);
    rsMatrixRotate(&matrix, 0, 1.0f, 0.0f, 0.0f); // Need to set this, even if not rotate
    rsgProgramVertexLoadModelMatrix(&matrix);
    
	// calculate the vertices
	calcVertices(gVx);
	  
    Bitmaps_t *b = bitmap;
    for (int j = 0; j < SEGMENT_H; j++) {
    	for (int i = 0; i < SEGMENT_W; i++) {
	    	rsgBindTexture(gProgFragmentTexture, 0, b->data);
	    	b++;
    		rsgDrawQuadTexCoords(
    			offset_x + vertices[i * 3 + j * (SEGMENT_W + 1) * 3]            , offset_y + vertices[i * 3 + j * (SEGMENT_W + 1) * 3 + 1]            , fabs(vertices[i * 3 + j * (SEGMENT_W + 1) * 3 + 2])             , 0, 1,
    			offset_x + vertices[i * 3 + (j + 1) * (SEGMENT_W + 1) * 3]      , offset_y + vertices[i * 3 + (j + 1) * (SEGMENT_W + 1) * 3 + 1]      , fabs(vertices[i * 3 + (j + 1) * (SEGMENT_W + 1) * 3 + 2])       , 0, 0,
    			offset_x + vertices[(i + 1) * 3 + (j + 1) * (SEGMENT_W + 1) * 3], offset_y + vertices[(i + 1) * 3 + (j + 1) * (SEGMENT_W + 1) * 3 + 1], fabs(vertices[(i + 1) * 3 + (j + 1) * (SEGMENT_W + 1) * 3 + 2]) , 1, 0,
    			offset_x + vertices[(i + 1) * 3 + j * (SEGMENT_W + 1) * 3]      , offset_y + vertices[(i + 1) * 3 + j * (SEGMENT_W + 1) * 3 + 1]      , fabs(vertices[(i + 1) * 3 + j * (SEGMENT_W + 1) * 3 + 2])       , 1, 1
    		);}
    }
}

static int initialized = 0;
static void initBitmaps() {
	Bitmaps_t *b = bitmap;
	b->data = gTex_00;
	b++;
	b->data = gTex_01;
	b++;
	b->data = gTex_02;
	b++;
	b->data = gTex_03;
	b++;
	b->data = gTex_04;
	b++;
}

int root(int launchID) {
	if (initialized == 0) {
		initBitmaps();
		initialized = 1;
	}
	
    //gDt = rsGetDt();
    rsgClearColor(0.2f, 0.2f, 0.2f, 0.0f);
    rsgClearDepth(1.0f);

    displayPageCurl();
    
    return 10;
}
