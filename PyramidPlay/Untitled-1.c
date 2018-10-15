#include <stdio.h>
#include <stdlib.h>
/*
  Original-Heapster
  Andrew Myer 012939730
  CECS 424
  10/11/18
*/
// overhead size of the block struct pointer
const int Block_overhead_size = sizeof(struct Block*);
// overhead size of the void pointer
const int void_overhead_size = sizeof(void*);
// pointer to the free head of the heap
struct Block *free_head;

/*
  blocks that make up the heap
  block_size: the size of the data portion of the block
  next_block: pointer to the next free block in the heap
*/
struct Block {
	int block_size;
	struct Block *next_block;
};

/*
  initializes the heap of a certain size and sets the freehead to the front of the heap
  size: the desired size of the data portion of the heap
*/
void my_initialize_heap(int size) {
  // allocates the size
  if(size>=Block_overhead_size+void_overhead_size){
    free_head =(struct Block*) malloc(size);
    // set free_head in the new heap
    free_head->block_size=size-Block_overhead_size;
    free_head->next_block=NULL;
  }
}

/*
  allocates a block of size size and determines where in the heap is should be placed
  size:the desired size of the data portion of the heap

*/
void* my_alloc(int size){
  // MUST round size up if is not amultiple of void_overhead_size
  if(size>0){
    while(size%void_overhead_size!=0){
      size++;
    }
    struct Block* freeWalker=free_head;
    struct Block* past_block=NULL;
    
    while(freeWalker->block_size<size){
      // tree walk if necessary
      if(freeWalker->next_block==NULL){
        return 0;
      }
      past_block=freeWalker;
      freeWalker=freeWalker->next_block;
    }
    
    if((freeWalker->block_size-size)>=(void_overhead_size+Block_overhead_size)){
      // split
      struct Block* new_free_block=(struct Block *)((char*)freeWalker+size+Block_overhead_size);
      // determine the location of the new free block
      new_free_block->block_size=freeWalker->block_size-(size+Block_overhead_size);
      // change the size of the allocated block to reflect the requested size
      freeWalker->block_size=size;  
      // change pointers 
      if(past_block==NULL){
        // new free block is now the free head
        free_head=new_free_block;
      } 
      else{
        // point the last free block to the current
        past_block->next_block=new_free_block;
      } 
    }
    else{
      // cant' split
      if(past_block==NULL){
        // the next block in the free list is the head
        free_head=freeWalker->next_block;
      }
      else{
        // link the past block to the next block
        past_block->next_block=freeWalker->next_block;
      }
    }
    // returns the data portion of the allocated block
    return (struct Block *)((char*)freeWalker+Block_overhead_size);
  }
 return 0; 
}

/*
  frees the block data from the heap
  data: the block to be deallocated
*/
void my_free(void *data){
  // sets the block to be freed equal to the address of data
  struct Block* block=(struct Block *)((char*)data-Block_overhead_size);
  // points the next block of the recently freed block to the current free head
  block->next_block=free_head;
  // the free block is now the new free head
  free_head=block;
}

/*
  checks to see if an int block is allocated to the same location if freed up first
*/
void test_1(){
  printf("testing case 1:\n");
  my_initialize_heap(10000);
	struct Block* block=my_alloc(sizeof(int));
	printf("block1 address: %p\n",block);
  my_free(block);
  struct Block* block2=my_alloc(sizeof(int));
  printf("block2 address: %p\n",block2);
  if(block==block2){
    printf("test 1 passed\n\n");
  }
  else{
     printf("test 1 failed\n\n");
  }
}

/*
  checks to see if two ints are allocated correctly without removing anything
*/
void test_2(){
  printf("testing case 2:\n");
  my_initialize_heap(10000);
	struct Block* block=my_alloc(sizeof(int));
	printf("block1 address: %p\n",block);
  struct Block* block2=my_alloc(sizeof(int));
  printf("block2 address: %p\n",block2);
  if(block2==(struct Block *)((char*)block+Block_overhead_size+void_overhead_size)){
    printf("test 2 passed\n\n");
  }
  else{
    printf("test 2 failed\n\n");
  }
}

/*
  allocates 3 ints then frees the second one, adds a double, and an int. Check to see that the double
  is at the end and the new int is where the removed int was
*/
void test_3(){
  printf("testing case 3:\n");
  my_initialize_heap(10000);
	struct Block* block=my_alloc(sizeof(int));
	printf("block1: %p\n",block);
  struct Block* block2=my_alloc(sizeof(int));
  printf("block2: %p\n",block2);
  struct Block* block3=my_alloc(sizeof(int));
  printf("block3: %p\n",block3);
  my_free(block2);
  struct Block* block4=my_alloc(2*sizeof(double));
  printf("block4: %p\n",block4);
  struct Block* new_block2=my_alloc(sizeof(int));
  printf("new_block_2: %p\n",new_block2);
  if(block2==(struct Block *)((char*)block+Block_overhead_size+void_overhead_size)&&(block3==(struct Block *)((char*)block2+Block_overhead_size+void_overhead_size))&&(block4==(struct Block *)((char*)block3+Block_overhead_size+void_overhead_size))&&(new_block2==block2)){
    printf("test 3 passed\n\n");
  }
  else{
    printf("test 3 failed\n\n");
  }
}

/*
  allocates one char and one int. checks to see if they are the same distance as the example from test 2
*/
void test_4(){
  printf("testing case 4:\n");
  my_initialize_heap(10000);
	struct Block* blocka=my_alloc(sizeof(int));
  struct Block* blockb=my_alloc(sizeof(int));

  my_initialize_heap(10000);
	struct Block* block1=my_alloc(sizeof(char));
	printf("address: %p\n",block1);
  struct Block* block2=my_alloc(sizeof(int));
  printf("address: %p\n",block2);
  printf("distance in test case 2: %p\n",(struct Block *)((char*)block2-(char*)block1));
  printf("distance in test case 4: %p\n",(struct Block *)((char*)blockb-(char*)blocka));
  if((struct Block *)((char*)block2-(char*)block1)==(struct Block *)((char*)blockb-(char*)blocka)){
    printf("test 4 passed\n\n");
  }
  else{
    printf("test 4 failed\n\n");
  }
}

/*
  allocates an array of 80 ints and allocates another int.See's if the int was allocated correctly after the array, then frees up the array and see if the int is in the same location.
*/
void test_5(){
  printf("testing case 5:\n");
  my_initialize_heap(100000);
  struct Block* block=my_alloc(80*sizeof(int));
  struct Block* block2=my_alloc(sizeof(int));
  printf("block2 address before my_free(block): %p\n",block);
  struct Block* temp=block2;
  my_free(block);
  printf("block2 address after my_free(block): %p\n",block);
  if(temp==block2&&block2==(struct Block *)((char*)block+Block_overhead_size+80*sizeof(int))){
    printf("test 5 passed\n\n");
  }
  else{
    printf("test 5 failed\n\n");
  }
}

/*
  runs all 5 tests
*/
int main(){
  test_1();
  test_2();
  test_3();
  test_4();
  test_5();
	return 0;
}
