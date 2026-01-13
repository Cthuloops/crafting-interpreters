#ifndef clox_value_h
#define clox_value_h

#include "common.h"

typedef double Value;

struct ValueArray {
    int capacity;
    int count;
    Value *values;
};

void init_value_array(struct ValueArray *array);
void write_value_array(struct ValueArray *array, Value value);
void free_value_array(struct ValueArray *array);
void print_value(Value value);

#endif  // clox_value_h