import os
import csv
import json
from collections import OrderedDict, defaultdict

import matplotlib.pyplot as plt

from py4j.java_gateway import JavaGateway

from learning_matrix import LearningMatrix


def transform_to_java_array(gateway, mb):
    java_array = gateway.jvm.java.util.ArrayList()
    for row in mb:
        java_array.append(gateway.jvm.java.util.ArrayList())
        for value in row:
            java_array[-1].append(int(value))
    return java_array


def load_arff_dataset(ds_dir, ds_name):
    test_ds = LearningMatrix()
    test_ds.from_arff(ds_dir + "/" + ds_name)
    return test_ds.get_basic_matrix()


def compare_datasets():
    gateway = JavaGateway()
    algorithms = gateway.entry_point

    dataset_dir = 'datasets/uci/'
    res_set = set()
    result_data = {}

    path_to_data = 'results/datasets'

    for ds_name in os.listdir(dataset_dir):
        curr_ds = ds_name.split(',')[0]
        result_data[curr_ds] = {}
        temp_data = {}
        res_set.clear()
        print("=========>", curr_ds)
        current_bm = load_arff_dataset(dataset_dir, ds_name)
        ones = 0
        for row in current_bm:
            for val in row:
                if val != 0:
                    ones += 1
        print("Rows: ", len(current_bm), " Columns: ", len(current_bm[0]))
        java_array_mb = transform_to_java_array(gateway, current_bm)
        res = str(algorithms.testTTAlgs(java_array_mb))
        res = res.replace("'", '"')
        result = json.loads(res)

        result_data[curr_ds]['density'] = ones / (len(current_bm) * len(current_bm[0]))

        for alg_name in result:
            if alg_name not in result_data[curr_ds]:
                result_data[curr_ds][alg_name] = {
                    'Hits': result[alg_name]['hits'],
                    'Time': result[alg_name]['ms_time']
                }
            res_set.add(result[alg_name]['numTT'])

        print(result_data[curr_ds])

    with open(path_to_data + '_result.txt', 'w') as file:
        file.write(json.dumps(result_data, indent=2))


def load_generated_dataset(ds_name):
    ds_location = 'datasets/generated/'
    my_dataset = ''
    if os.path.exists(ds_location + ds_name + '.json'):
        my_dataset = ds_location + ds_name + '.json'
    if my_dataset == '':
        raise Exception('Cannot find dataset')
    with open(my_dataset, 'r') as file:
        curr_dataset = json.load(file)
        sort_kes = []
        for key in curr_dataset:
            sort_kes.append(key)
        sort_kes.sort()
        data = OrderedDict()
        for key in sort_kes:
            data[key] = curr_dataset[key]
        return data


def compare_generated(att, dataset_name):
    gateway = JavaGateway()
    algorithms = gateway.entry_point

    path_to_data = 'results/' + dataset_name
    density_dataset = load_generated_dataset(dataset_name)

    data = defaultdict(dict)
    dims = []
    res_set = set()
    for density in density_dataset:
        dims.append(density)
        temp_data = {}
        for curr_mb in density_dataset[density]:
            res_set.clear()
            java_array_mb = transform_to_java_array(gateway, curr_mb)
            res = str(algorithms.testTTAlgs(java_array_mb))
            res = res.replace("'", '"')
            result = json.loads(res)
            for alg_name in result:
                if alg_name not in temp_data:
                    temp_data[alg_name] = {
                        'Hits': [],
                        'Time': []
                    }
                res_set.add(result[alg_name]['numTT'])
                temp_data[alg_name]['Hits'].append(result[alg_name]['hits'])
                temp_data[alg_name]['Time'].append(result[alg_name]['ms_time'])
            if len(res_set) > 1:
                for alg_name in result:
                    print(alg_name, result[alg_name]['numTT'])
                print("Different number of testors", res_set)
        for alg_name in temp_data:
            if alg_name not in data:
                data[alg_name] = {
                    'Hits': [],
                    'Time': []
                }
            data[alg_name]['Hits'].append(sum(temp_data[alg_name]['Hits']) / len(temp_data[alg_name]['Hits']))
            data[alg_name]['Time'].append(sum(temp_data[alg_name]['Time']) / len(temp_data[alg_name]['Time']))
        print("End density ", density)

    # save data
    with open(path_to_data + '_data.txt', 'w') as file:
        file.write(json.dumps(data, indent=2))

    # defined for 5 different algorithms
    marker = ['rs-', 'go-', 'b>-', 'y*-', 'ph-']
    for pos, alg_name in enumerate(data):
        plt.plot(dims, data[alg_name][att], marker[pos], label=alg_name)

    y_label = 'Evaluated sets (hits)'
    if att == "Time":
        y_label = 'Time (ms)'
    plt.ylabel(y_label)
    plt.xlabel("D(1)")
    plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.1), ncol=4)
    plt.savefig(path_to_data + '_' + att + '.png')
    plt.close()

    print("END")


if __name__ == '__main__':
    # att = 'Hits'
    att = 'Time'
    dataset_name = "250_20"
    compare_generated(att, dataset_name)
    compare_datasets()
